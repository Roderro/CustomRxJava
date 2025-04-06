package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.mockito.Mockito.*;

public class FilterObserverTest extends AbstractObserverTest {
    private final Predicate<Integer> FILTER = x -> x % 2 == 0;

    @BeforeEach
    void childSetup() {
        testObserver = new FilterObserver<>(disposable, downstream, FILTER);
    }

    @Test
    @Override
    @DisplayName("Фильтрует значения: передает только четные числа")
    void observerShouldLowerNext() {
        testObserver.onNext(1);
        verify(downstream, never()).onNext(1);
        testObserver.onNext(2);
        verify(downstream, times(1)).onNext(2);
    }

    @Test
    @Override
    @DisplayName("Передает отфильтрованные значения и onComplete")
    void observerShouldLowerAllEvents() {
        testObserver.onNext(2);
        testObserver.onNext(4);
        testObserver.onComplete();

        // Assert
        verify(downstream, times(2)).onNext(any());
        verify(downstream, times(1)).onComplete();
        verify(downstream, never()).onError(any());
    }


    @Test
    @Override
    @DisplayName("Передает ошибку, если фильтрация вызывает исключение")
    void shouldHandleErrorOnNext() {
        RuntimeException error = new RuntimeException();
        doAnswer(invocation -> {
            throw error;
        }).when(downstream).onNext(any());

        testObserver.onNext(2);
        verify(downstream, times(1)).onError(error);
    }
}
