package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sf.hw.observable.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FlatMapObserverTest extends AbstractObserverTest {
    private final int COUNT_ON_NEXT = 3;
    private final Function<Integer, Observable<Integer>> MAPPER = val -> {
        Observable<Integer> observable = Mockito.mock(Observable.class);
        doAnswer(inc -> {
            Observer<Integer> observer = inc.getArgument(0);
            for (int i = 0; i < COUNT_ON_NEXT; i++) {
                observer.onNext(i + 1);
            }
            return null;
        }).when(observable).subscribe(any(Observer.class));
        return observable;
    };

    @BeforeEach
    void childSetup() {
        testObserver = new FlatMapObserver<>(disposable, downstream, MAPPER, new ConcurrentHashMap<>());
    }

    @Test
    @Override
    @DisplayName("Преобразует значение в новый Observable и передает все элементы")
    void observerShouldLowerNext() {
        testObserver.onNext(1);
        verify(downstream, times(COUNT_ON_NEXT)).onNext(any());
    }

    @Test
    @Override
    @DisplayName("Преобразует каждое значение в новый Observable, передает все элементы и завершает работу")
    void observerShouldLowerAllEvents() {
        testObserver.onNext(1);
        testObserver.onNext(2);
        testObserver.onComplete();

        // Assert
        verify(downstream, times(COUNT_ON_NEXT * 2)).onNext(any());
        verify(downstream, times(1)).onComplete();
        verify(downstream, never()).onError(any());
    }

    @Test
    @DisplayName("Передает ошибку, если маппер выбрасывает исключение")
    void shouldHandleMapperError() {
        // Arrange
        RuntimeException error = new RuntimeException("Mapper failed");
        Function<Integer, Observable<Integer>> failingMapper = i -> {
            throw error;
        };
        testObserver = new FlatMapObserver<>(disposable, downstream, failingMapper, new ConcurrentHashMap<>());

        // Act
        testObserver.onNext(1);

        // Assert
        verify(downstream, times(1)).onError(error);
        verify(downstream, never()).onNext(any());
        verify(downstream, never()).onComplete();
    }


    @Test
    @DisplayName("Корректно работает с реальными Observable (проверка цепочки)")
    void shouldWorkWithRealObservables() {
        // Arrange
        List<Integer> results = new ArrayList<>();
        Observer<Integer> collectingObserver = new Observer<>() {
            public void onNext(Integer i) {
                results.add(i);
            }

            public void onError(Throwable t) {
                fail("Unexpected error");
            }

            public void onComplete() {
                results.add(10);
            }
        };

        testObserver = new FlatMapObserver<>(
                disposable,
                collectingObserver,
                i -> Observable.create(emitter -> {
                    emitter.onNext(i);
                    emitter.onNext(i + 1);
                }),
                new ConcurrentHashMap<>()
        );

        // Act
        testObserver.onNext(1);
        testObserver.onNext(2);
        testObserver.onComplete();

        // Assert
        assertThat(results).containsExactly(1, 2, 2, 3, 10);
    }

    @Test
    @DisplayName("Игнорирует пустые Observable")
    void shouldHandleEmptyObservables() {
        // Arrange
        Function<Integer, Observable<Integer>> emptyMapper =
                val -> {
                    Observable<Integer> observable = Mockito.mock(Observable.class);
                    doNothing().when(observable).subscribe(any(Observer.class));
                    return observable;
                };

        testObserver = new FlatMapObserver<>(disposable, downstream, emptyMapper, new ConcurrentHashMap<>());

        // Act
        testObserver.onNext(1);
        testObserver.onComplete();

        // Assert
        verify(downstream, never()).onNext(any());
        verify(downstream, times(1)).onComplete();
    }

}
