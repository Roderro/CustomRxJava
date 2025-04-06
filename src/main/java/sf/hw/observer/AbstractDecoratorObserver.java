package sf.hw.observer;

import org.slf4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractDecoratorObserver<T> implements Observer<T> {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private final int id = ID_GENERATOR.getAndIncrement();
    protected final Disposable disposable;
    protected volatile boolean done = false;
    protected Logger log;

    public AbstractDecoratorObserver(Disposable disposable,Logger log) {
        this.disposable = disposable;
        this.log = log;
    }

    @Override
    public void onNext(T item) {
        if (isDisposed() || done) {
            return;
        }
        log.debug("Method onNext started");
        if (item == null) {
            log.error("onNext called with null");
            onError(new NullPointerException("onNext called with null"));
            return;
        }
        try {
            actionsAtOnNext(item);
        } catch (Exception e) {
            onError(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        if (isDisposed() || done) {
            return;
        }
        if (t == null) {
            log.error("onError called with null");
            t = new NullPointerException("onError called with null");
        }
        log.debug("Method onError started");
        done = true;
        actionsAtOnError(t);
    }

    @Override
    public void onComplete() {
        if (isDisposed() || done) {
            return;
        }
        log.debug("Method onComplete started");
        done = true;
        actionsAtOnComplete();
    }

    protected abstract void actionsAtOnNext(T item);

    protected abstract void actionsAtOnError(Throwable t);

    protected abstract void actionsAtOnComplete();


    public boolean isDisposed() {
        if (disposable.isDisposed()) {
            log.debug("Observer has been disposed");
            return true;
        }
        return false;
    }

    public void dispose() {
        disposable.dispose();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDecoratorObserver<?> that = (AbstractDecoratorObserver<?>) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
