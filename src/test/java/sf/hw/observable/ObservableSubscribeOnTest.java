package sf.hw.observable;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import sf.hw.observer.Observer;
import sf.hw.scheduler.Scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class ObservableSubscribeOnTest extends AbstractObservableTest {
    private final Scheduler scheduler = getScheduler();

    @Override
    Class<? extends Observer> getExpectedWrapperClass() {
        return observer.getClass();
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableSubscribeOn<>(parentObservable, scheduler);
    }
}
