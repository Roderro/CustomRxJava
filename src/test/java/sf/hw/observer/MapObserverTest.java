package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MapObserverTest extends AbstractObserverTest {
    private final Function<Integer, Integer> FUNCTION = x -> x * 2;

    @BeforeEach
    void childSetup() {
        testObserver = new MapObserver<>(disposable, downstream, FUNCTION);
    }

    @Test
    @DisplayName("Применяет функцию преобразования к каждому значению")
    void observerShouldLowerMapNext(){
        testObserver.onNext(1);
        verify(downstream, times(1)).onNext(2);
    }
}

