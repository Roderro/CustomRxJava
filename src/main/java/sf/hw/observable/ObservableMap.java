package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.MapObserver;
import sf.hw.observer.Observer;

import java.util.function.Function;

class ObservableMap<T, R> extends Observable<R> {
    private final Function<? super T, ? extends R> mapper;
    private final Observable<T> parent;


    public ObservableMap(Observable<T> parent, Function<? super T, ? extends R> mapper) {
        super(parent.getDisposables());
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public void subscribeActual(Observer<? super R> observer) {
        Disposable disposable = getDisposable(observer);
        MapObserver<T, R> mapObserver = new MapObserver<>(disposable, observer, mapper);
        chainDisposable(disposable, (Observer<? super R>) mapObserver);
        parent.subscribeActual(mapObserver);
    }
}
