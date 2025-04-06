package sf.hw.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOScheduler implements Scheduler {
    private final ExecutorService executor;

    public IOScheduler() {
        this.executor = Executors.newCachedThreadPool(new SchedulerTreadFactory("IO"));
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

}
