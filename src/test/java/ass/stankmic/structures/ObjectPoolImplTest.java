package ass.stankmic.structures;

import java.lang.Thread.State;
import java.util.Collection;
import java.util.HashSet;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ObjectPoolImplTest {

    private ObjectPool TESTED;

    @Before
    public void setUp() {
        TESTED = new ObjectPoolImpl();
    }

    @After
    public void tearDown() {
        TESTED = null;
    }

    @Test
    public void offerPollSimpleTest() throws InterruptedException {
        Object offered = new Object();
        TESTED.offer(offered);
        Object polled = TESTED.poll();
        assertEquals("Polled Object does not equal to offered one.", offered, polled);
    }

    @Test
    public synchronized void testPollingThreadIsBlocked() throws InterruptedException {
        Thread polling = new Thread(new Polling(new HashSet()));
        polling.start();
        this.wait(1000);
        State state = polling.getState();
        assertEquals("The thread has not been blocked when pool is empty.", State.WAITING, state);
    }

    @Test
    public void pollingThreadBeforeOfferTest() throws InterruptedException {
        Object offered = new Object();
        Collection polled = new HashSet();
        Thread polling = new Thread(new Polling(polled));
        polling.start();
        Thread offering = new Thread(new Offering(offered));
        offering.start();
        polling.join();
        offering.join();
        assertEquals("Polled Object does not equal to offered one.", true, polled.contains(offered));
    }

    private class Polling implements Runnable {

        private final Collection polled;

        public Polling(Collection result) {
            this.polled = result;
            this.polled.clear();
        }

        public void run() {
            try {
                polled.add(TESTED.poll());
            } catch (InterruptedException ex) {
                // WHO CARES?
            }
        }

    }

    private class Offering implements Runnable {

        private final Object toOffer;

        public Offering(Object toOffer) {
            this.toOffer = toOffer;
        }

        public void run() {
            TESTED.offer(toOffer);
        }

    }
}
