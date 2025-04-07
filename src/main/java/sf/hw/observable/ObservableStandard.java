package sf.hw.observable;


import sf.hw.observer.Observer;

import java.util.concurrent.ConcurrentHashMap;

public final class ObservableStandard<T> extends Observable<T> {
    private final Emitting<T> source;

    ObservableStandard(Emitting<T> source) {
        super(new ConcurrentHashMap<>());
        this.source = source;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        try {
            source.emit(observer);
        } catch (Throwable ex) {
            observer.onError(ex);
        }
    }

}