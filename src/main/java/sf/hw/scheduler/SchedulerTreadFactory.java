package sf.hw.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerTreadFactory implements ThreadFactory {
    AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public SchedulerTreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + "-scheduler-worker-" + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
