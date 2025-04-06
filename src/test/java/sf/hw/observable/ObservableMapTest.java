package sf.hw.observable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sf.hw.observer.MapObserver;
import sf.hw.observer.Observer;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class ObservableMapTest extends AbstractObservableTest {
    private final Function<String, String> FUNCTION = Function.identity();

    @Override
    protected Class<? extends Observer> getExpectedWrapperClass() {
        return MapObserver.class;
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableMap<>(parentObservable, FUNCTION);
    }

    @Test
    @DisplayName("Map применяет функцию к каждому значению")
    void map_shouldApplyFunction() {
        // Arrange
        Function<String, String> upperCaseMapper = String::toUpperCase;
        testObservable = new ObservableMap<>(parentObservable, upperCaseMapper);
        AtomicReference<String> lastValue = new AtomicReference<>();
        Observer<String> testObserver = new Observer<>() {
            @Override
            public void onNext(String value) {
                lastValue.set(value);
            }

            @Override
            public void onError(Throwable throwable) {
                fail("Unexpected error");
            }

            @Override
            public void onComplete() {
                // do nothing
            }
        };

        // Act
        testObservable.subscribe(testObserver);

        // Assert
        assertEquals("WORLD", lastValue.get(), "Значение должно быть преобразовано в верхний регистр");
    }
}
