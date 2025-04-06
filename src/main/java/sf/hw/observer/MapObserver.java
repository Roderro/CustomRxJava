package sf.hw.observer;

import org.slf4j.LoggerFactory;

import java.util.function.Function;


public class MapObserver<T, R> extends AbstractDecoratorObserver<T> {
    private final Function<? super T, ? extends R> mapper;
    private final Observer<? super R> downstream;

    public MapObserver(Disposable disposable, Observer<? super R> downstream, Function<? super T, ? extends R> mapper) {
        super(disposable, LoggerFactory.getLogger(MapObserver.class));
        this.downstream = downstream;
        this.mapper = mapper;
    }

    @Override
    protected void actionsAtOnNext(T item) {
        downstream.onNext(mapper.apply(item));
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
