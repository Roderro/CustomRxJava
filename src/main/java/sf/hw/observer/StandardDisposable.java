package sf.hw.observer;

public class StandardDisposable implements Disposable {
    volatile boolean disposed = false;

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}

