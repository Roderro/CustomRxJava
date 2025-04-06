package sf.hw.observable;


import sf.hw.observer.Observer;
import sf.hw.scheduler.Scheduler;


class ObservableSubscribeOn<T> extends Observable<T> {
    final Observable<T> parent;
    final Scheduler scheduler;

    ObservableSubscribeOn(Observable<T> parent, Scheduler scheduler) {
        super(parent.getDisposables());
        this.parent = parent;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        scheduler.execute(() -> parent.subscribeActual(observer));
    }
}