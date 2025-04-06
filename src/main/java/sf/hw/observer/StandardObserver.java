package sf.hw.observer;

import org.slf4j.LoggerFactory;


public class StandardObserver<T> extends AbstractDecoratorObserver<T> {
    final Observer<? super T> downstream;

    public StandardObserver(Disposable disposable, Observer<? super T> downstream) {
        super(disposable);
        this.downstream = downstream;
        this.log = LoggerFactory.getLogger(StandardObserver.class);
    }


    @Override
    protected void actionsAtOnNext(T item) {
        downstream.onNext(item);
    }

    @Override
    protected void actionsAtOnError(Throwable t) {
        downstream.onError(t);
    }


    @Override
    protected void actionsAtOnComplete() {
        downstream.onComplete();
    }
}