package ass.stankmic.structures;

/**
 * Implementing this interface should ensure to reuse limited number of Threads
 * to run more Runnables.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public interface WorkerPool {

    /**
     * Give this method a Runnable instance to run it when some Thread will be
     * available - no guarantee, when it happens.
     *
     * @param runnable Runnable to run when some Thread will be available
     */
    public void run(final Runnable runnable);

    /**
     * Remove all Runnables from the queue.
     */
    public void flush();
}
