package sf.hw.observable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sf.hw.observer.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

public class ObservableStandardTest extends AbstractObservableTest {


    @Override
    protected Class<? extends Observer> getExpectedWrapperClass() {
        return StandardObserver.class;
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableStandard<>(emittingSource);
    }

    @Test
    @DisplayName("Проверка обертки Observer в StandardObserver")
    @Override
    void shouldWrapObserver() {
        Emitting<String> emitting = Mockito.mock(Emitting.class);
        ObservableStandard<String> testObservable = new ObservableStandard<>(emitting);
        testObservable.subscribe(observer);
        verify(emitting)
                .emit(argThat(arg -> getExpectedWrapperClass().isInstance(arg)));
    }

}
