package sf.hw.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sf.hw.observable.Observable;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


class AutoUnsubscribeObserverTest extends AbstractObserverTest {
    private Observable<Integer> observable = Mockito.mock(Observable.class);

    @BeforeEach
    void beforeEach() {
        testObserver = new AutoUnsubscribeObserver<>(disposable, downstream, observable);
    }

    @Test
    @DisplayName("Вызывает отписку при OnComplete")
    void callsAnUnsubscriptionAtOnComplete() {
        testObserver.onComplete();
        verify(downstream, times(1)).onComplete();
        verify(observable, times(1)).unsubscribe(downstream);
    }

    @Test
    @DisplayName("Вызывает отписку при OnError")
    void callsAnUnsubscriptionAtOnError() {
        RuntimeException error = new RuntimeException();
        testObserver.onError(error);
        verify(downstream, times(1)).onError(error);
        verify(observable, times(1)).unsubscribe(downstream);
    }

}
