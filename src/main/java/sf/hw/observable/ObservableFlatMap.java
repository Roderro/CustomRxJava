package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.FlatMapObserver;
import sf.hw.observer.Observer;

import java.util.function.Function;

class ObservableFlatMap<T, R> extends Observable<R> {
    final Observable<T> source;
    final Function<? super T, ? extends Observable<? extends R>> mapper;

    ObservableFlatMap(Observable<T> source, Function<? super T, ? extends Observable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribeActual(Observer<? super R> observer) {
        Disposable disposable = getDisposable(observer);
        FlatMapObserver<? super T, R> observerFlatMap = new FlatMapObserver<>(disposable, observer, mapper,disposableMap);
        chainDisposable(disposable, (Observer<? super R>) observerFlatMap);
        source.subscribeActual(observerFlatMap);
    }
}
