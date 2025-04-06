package sf.hw.observer;

import org.slf4j.LoggerFactory;

import java.util.function.Predicate;


public class FilterObserver<T> extends AbstractDecoratorObserver<T> {
    private final Predicate<? super T> filter;
    private final Observer<? super T> downstream;

    public FilterObserver(Disposable disposable,Observer<? super T> downstream, Predicate<? super T> filter) {
        super(disposable,LoggerFactory.getLogger(FilterObserver.class));
        this.downstream = downstream;
        this.filter = filter;
    }


    @Override
    protected void actionsAtOnNext(T item) {
        if(filter.test(item)) {
            downstream.onNext(item);
        }
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
