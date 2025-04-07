package sf.hw.observable;


import sf.hw.observer.AutoUnsubscribeObserver;
import sf.hw.observer.Observer;
import sf.hw.scheduler.Scheduler;



public class ObservableSubscribeOnTest extends AbstractObservableTest {
    private final Scheduler scheduler = getScheduler();

    @Override
    Class<? extends Observer> getExpectedWrapperClass() {
        return AutoUnsubscribeObserver.class;
    }

    @Override
    Observable<String> getTestObservable() {
        return new ObservableSubscribeOn<>(parentObservable, scheduler);
    }
}
