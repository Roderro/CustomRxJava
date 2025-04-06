package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.FilterObserver;
import sf.hw.observer.Observer;

import java.util.function.Predicate;

public class ObservableFilter<T> extends Observable<T> {
    private final Predicate<? super T> filter;
    private final Observable<T> parent;

    protected ObservableFilter(Observable<T> parent, Predicate<? super T> filter) {
        this.parent = parent;
        this.filter = filter;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        Disposable disposable = getDisposable(observer);
        Observer<T> filterObserver = new FilterObserver<>(disposable, observer, filter);
        chainDisposable(disposable, filterObserver);
        parent.subscribeActual(filterObserver);
    }
}
