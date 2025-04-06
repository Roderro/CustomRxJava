package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.ObserveOnObserver;
import sf.hw.observer.Observer;
import sf.hw.scheduler.Scheduler;

class ObservableObserveOn<T> extends Observable<T> {
    final Observable<T> parent;
    final Scheduler scheduler;

    ObservableObserveOn(Observable<T> parent, Scheduler scheduler) {
        this.parent = parent;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        Disposable disposable = getDisposable(observer);
        Observer<T> onObserver = new ObserveOnObserver<>(disposable, observer, scheduler);
        chainDisposable(disposable, onObserver);
        parent.subscribeActual(onObserver);
    }
}