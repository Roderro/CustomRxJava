package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.FlatMapObserver;
import sf.hw.observer.Observer;

import java.util.function.Function;

class ObservableFlatMap<T, R> extends Observable<R> {
    final Observable<T> parent;
    final Function<? super T, ? extends Observable<? extends R>> mapper;

    ObservableFlatMap(Observable<T> parent, Function<? super T, ? extends Observable<? extends R>> mapper) {
        super(parent.getDisposables());
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public void subscribeActual(Observer<? super R> observer) {
        Disposable disposable = getDisposable(observer);
        FlatMapObserver<? super T, R> observerFlatMap = new FlatMapObserver<>(disposable, observer, mapper, getDisposables());
        chainDisposable(disposable, (Observer<? super R>) observerFlatMap);
        parent.subscribeActual(observerFlatMap);
    }
}
