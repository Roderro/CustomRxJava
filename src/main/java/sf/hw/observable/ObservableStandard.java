package sf.hw.observable;

import sf.hw.observer.Disposable;
import sf.hw.observer.StandardObserver;
import sf.hw.observer.Observer;

public final class ObservableStandard<T> extends Observable<T> {
    private final Emitting<T> source;

    ObservableStandard(Emitting<T> source) {
        this.source = source;
    }


    @Override
    public void subscribeActual(Observer<? super T> observer) {
        Disposable disposable = getDisposable(observer);
        Observer<T> standardObserver = new StandardObserver<>(disposable, observer);
        chainDisposable(disposable, standardObserver);
        try {
            source.emit(standardObserver);
        } catch (Throwable ex) {
            standardObserver.onError(ex);
        }
    }
}