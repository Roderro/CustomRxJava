package sf.hw.observable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sf.hw.observer.FilterObserver;
import sf.hw.observer.Observer;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ObservableFilterTest extends AbstractObservableTest {
    private final Predicate<String> FILTER = t -> true;



    @Override
    protected Class<? extends Observer> getExpectedWrapperClass() {
        return FilterObserver.class;
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableFilter<>(parentObservable, FILTER);
    }

    @Test
    @DisplayName("Filter применяет функцию к каждому значению")
    void filter_shouldApplyFunction() {
        // Arrange
        Predicate<String> filter = str->str.startsWith("H");
        testObservable = new ObservableFilter<>(parentObservable, filter);
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
        assertEquals("Hello", lastValue.get(), "Filter should allow 'Hello'");
    }
}
