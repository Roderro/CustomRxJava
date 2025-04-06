package sf.hw.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {
    private final ExecutorService executor;
    private final int threads;

    public ComputationScheduler(int threads) {
        this.threads = threads;
        this.executor = Executors.newFixedThreadPool(threads,new SchedulerTreadFactory("Computation"));
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

}
