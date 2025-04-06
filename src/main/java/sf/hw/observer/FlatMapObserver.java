package sf.hw.observer;

import org.slf4j.LoggerFactory;
import sf.hw.observable.Observable;

import java.util.Map;
import java.util.function.Function;


public class FlatMapObserver<T, R> extends AbstractDecoratorObserver<T> {
    final Observer<? super R> downstream;
    final Map<Observer<?>, Disposable> disposableMap;
    final Function<? super T, ? extends Observable<? extends R>> mapper;

    public FlatMapObserver(Disposable disposable, Observer<? super R> downstream,
                           Function<? super T, ? extends Observable<? extends R>> mapper,
                           Map<Observer<?>, Disposable> disposableMap) {
        super(disposable,LoggerFactory.getLogger(FlatMapObserver.class));
        this.downstream = downstream;
        this.mapper = mapper;
        this.disposableMap = disposableMap;
    }

    @Override
    protected void actionsAtOnNext(T item) {
        try {
            Observable<? extends R> observable = mapper.apply(item);
            Observer<? super R> observer = new StandardObserverDoNothingOnComplete<R>(disposable, downstream);
            disposableMap.put(observer, disposable);
            observable.subscribe(observer);
        } catch (Throwable ex) {
            onError(ex);
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

    static class StandardObserverDoNothingOnComplete<R> extends AbstractDecoratorObserver<R> {
        private final Observer<? super R> downstream;

        public StandardObserverDoNothingOnComplete(Disposable disposable, Observer<? super R> downstream) {
            super(disposable,LoggerFactory.getLogger(StandardObserverDoNothingOnComplete.class));
            this.downstream = downstream;
        }

        @Override
        protected void actionsAtOnNext(R item) {
            downstream.onNext(item);
        }

        @Override
        protected void actionsAtOnError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        protected void actionsAtOnComplete() {
            // Ничего не делаем, ждем завершения основного потока
        }
    }

}
