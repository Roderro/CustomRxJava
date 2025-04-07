package sf.hw.observer;


import org.slf4j.LoggerFactory;
import sf.hw.observable.Observable;

public class AutoUnsubscribeObserver<T> extends AbstractDecoratorObserver<T> {
    private final Observer<? super T> downstream;
    private final Observable<T> observable;

    public AutoUnsubscribeObserver(Disposable disposable, Observer<? super T> downstream, Observable<T> observable) {
        super(disposable, LoggerFactory.getLogger(AutoUnsubscribeObserver.class));
        this.downstream = downstream;
        this.observable = observable;
    }

    @Override
    protected void actionsAtOnNext(T item) {
        downstream.onNext(item);
    }

    @Override
    protected void actionsAtOnError(Throwable t) {
        try {
            downstream.onError(t);
        } finally {
            observable.unsubscribe(downstream);
        }
    }

    @Override
    protected void actionsAtOnComplete() {
        try {
            downstream.onComplete();
        } finally {
            observable.unsubscribe(downstream);
        }
    }
}
