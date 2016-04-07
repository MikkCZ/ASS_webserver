package ass.stankmic.structures;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This implementation of ObjectPool is safe to use by multiple threads - all
 * methods are synchronized and poll() is blocking.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 * @param <T> any class
 */
public class ObjectPoolImpl<T> implements ObjectPool<T> {

    private final Queue<T> objectQueue;

    /**
     * Contructor
     */
    protected ObjectPoolImpl() {
        this.objectQueue = new LinkedList();
    }

    public synchronized T poll() throws InterruptedException {
        while (objectQueue.isEmpty()) {
            wait();
        }
        return objectQueue.poll();
    }

    public synchronized void offer(final T object) {
        if (objectQueue.contains(object)) {
            return;
        }
        objectQueue.add(object);
        notifyAll();
    }

    public synchronized void clear() {
        objectQueue.clear();
    }
}
