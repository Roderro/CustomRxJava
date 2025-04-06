package sf.hw.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor;

    public SingleThreadScheduler() {
        this.executor = Executors.newSingleThreadExecutor(new SchedulerTreadFactory("SingleThread"));
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

}