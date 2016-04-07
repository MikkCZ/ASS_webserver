package ass.stankmic.structures;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the WorkerPoolImpl class
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class WorkerPoolImplTest {

    private WorkerPool TESTED;

    /**
     * Get instace of the WorkerPool.
     */
    @Before
    public void setUp() {
        TESTED = WorkerPoolImpl.getInstance();
        TESTED.flush();
    }

    /**
     * Flush the WorkerPool.
     */
    @After
    public void tearDown() {
        TESTED.flush();
        TESTED = null;
    }

    /**
     * Test finished job for one Runnable.
     *
     * @throws InterruptedException
     */
    @Test
    public synchronized void test1FinishedJob() throws InterruptedException {
        testFinishedJobs(1);
    }

    /**
     * Test finished jobs with 100 Runnables.
     *
     * @throws InterruptedException
     */
    @Test
    public synchronized void test100FinishedJobs() throws InterruptedException {
        testFinishedJobs(100);
    }

    /**
     * Test finished jobs for the given number of Runnables.
     *
     * @param number of Runnables to test with
     * @throws InterruptedException
     */
    private synchronized void testFinishedJobs(int number) throws InterruptedException {
        final Collection<Runnable> runnables = new ArrayList<>(number);
        final Collection<Job> jobs = new ArrayList<>(number);
        fillRunnables(runnables, jobs, number);
        for (Runnable r : runnables) {
            TESTED.run(r);
        }
        wait(number * 50);
        for (Job job : jobs) {
            assertTrue("The runnable seems to be not executed.", job.ready);
        }
    }

    /**
     * Prepare the given number of Runnables.
     *
     * @param runnables collection to fill with Runnables
     * @param jobs colletion to fill with Job classes
     * @param number of instances to prepare
     */
    private void fillRunnables(Collection<Runnable> runnables, Collection<Job> jobs, int number) {
        for (int i = 0; i < number; i++) {
            final Job result = new Job();
            jobs.add(result);
            runnables.add(new Runnable() {
                public void run() {
                    result.finish();
                }
            });
        }
    }

    private class Job {

        public boolean ready = false;

        public void finish() {
            ready = true;
        }
    }
}
