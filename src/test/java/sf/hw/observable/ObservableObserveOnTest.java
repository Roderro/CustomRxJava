package sf.hw.observable;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import sf.hw.observer.ObserveOnObserver;
import sf.hw.observer.Observer;
import sf.hw.scheduler.Scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class ObservableObserveOnTest extends AbstractObservableTest {
    private final Scheduler scheduler = getScheduler();

    @Override
    protected Class<? extends Observer> getExpectedWrapperClass() {
        return ObserveOnObserver.class;
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableObserveOn<>(parentObservable, scheduler);
    }

}
