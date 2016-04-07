package ass.stankmic.structures;

/**
 * Singleton implementation of the WorkerPool interface, where the number of
 * Threads is equals to the number of threads of the CPU.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class WorkerPoolImpl implements WorkerPool {

    private final int numOfThreads;
    private final Thread[] threads;
    private final ObjectPool<Runnable> runnablesPool = new ObjectPoolImpl();

    /**
     * Returns the instace of WorkerPoolImpl as WorkerPool.
     *
     * @return singleton WorkerPoolImpl implementation
     */
    public static WorkerPool getInstance() {
        return WorkerPoolImpl.WorkerPoolImplHolder.INSTANCE;
    }

    /**
     * Creates new WorkerPoolImpl with the number of Threads according to the
     * CPU number of threads.
     */
    private WorkerPoolImpl() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates new WorkerPoolImpl with the given number of Threads.
     */
    private WorkerPoolImpl(final int numOfThreads) {
        this.numOfThreads = numOfThreads;
        threads = new Thread[this.numOfThreads];
        initAndStartThreads();
    }

    private void initAndStartThreads() {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new WorkerRunnable());
            threads[i].start();
        }
    }

    public void run(Runnable runnable) {
        runnablesPool.offer(runnable);
    }

    public void flush() {
        runnablesPool.clear();
    }

    private static class WorkerPoolImplHolder {

        private static final WorkerPoolImpl INSTANCE = new WorkerPoolImpl();
    }

    /**
     * Runnable to be run in a pool Thread to run the enqueued Runnables.
     */
    private class WorkerRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                Runnable r;
                try {
                    r = runnablesPool.poll();
                } catch (InterruptedException ex) {
                    continue;
                }
                r.run();
            }
        }
    }
}
