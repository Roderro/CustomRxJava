package sf.hw.observer;


import org.slf4j.LoggerFactory;
import sf.hw.scheduler.Scheduler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public final class ObserveOnObserver<T> extends AbstractDecoratorObserver<T> implements Runnable {
    final Observer<? super T> downstream;
    final Scheduler schedule;
    final Queue<T> queue = new ConcurrentLinkedQueue<>();
    final AtomicBoolean isRunning = new AtomicBoolean(false);
    Throwable error;

    public ObserveOnObserver(Disposable disposable, Observer<? super T> downstream, Scheduler schedule) {
        super(disposable, LoggerFactory.getLogger(ObserveOnObserver.class));
        this.downstream = downstream;
        this.schedule = schedule;
    }


    @Override
    protected void actionsAtOnNext(T item) {
        queue.offer(item);
        schedule();
    }

    @Override
    protected void actionsAtOnError(Throwable t) {
        error = t;
        schedule();
    }

    @Override
    protected void actionsAtOnComplete() {
        schedule();
    }


    @Override
    public void run() {
        int emitted = 0;
        final int limit = 256; // Бач для предотвращения starvation

        while (true) {
            if (isDisposed()) {
                queue.clear();
                return;
            }

            T v = queue.poll();
            boolean empty = v == null;

            if (checkTerminated(empty)) {
                return;
            }
            if (empty) {
                break;
            }
            try {
                downstream.onNext(v);
            } catch (Throwable ex) {
                isRunning.set(false);
                onError(ex);
                return;
            }

            emitted++;
            if (emitted == limit) {
                // Чтобы дать другим задачам шанс выполниться
                isRunning.set(false);
                schedule();
                return;
            }
        }

        // Завершаем выполнение
        isRunning.set(false);
        // Проверяем, не появились ли новые элементы
        if (!queue.isEmpty()) {
            schedule();
        }
    }

    private void schedule() {
        if (isRunning.compareAndSet(false, true)) {
            schedule.execute(this);
        }
    }

    private boolean checkTerminated(boolean empty) {
        if (isDisposed()) {
            queue.clear();
            return true;
        }
        if (done) {
            if (error != null) {
                queue.clear();
                downstream.onError(error);
                return true;
            } else if (empty) {
                downstream.onComplete();
                return true;
            }
        }
        return false;
    }
}
