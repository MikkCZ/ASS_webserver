package ass.stankmic.structures;

/**
 * Object pools implementing this inteface should be safe for multiple threads.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 * @param <T> any class
 */
public interface ObjectPool<T> {

    /**
     * Return one object from the pool and removes it - should be blocking.
     *
     * @return object from the pool
     * @throws InterruptedException
     */
    public T poll() throws InterruptedException;

    /**
     * Add given object into the queue and notify Threads blocked in poll().
     *
     * @param object object to place in the pool
     */
    public void offer(final T object);

    /**
     * Remove all objects from the pool.
     */
    public void clear();
}
