package ass.stankmic.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JEditorPane;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Main.java class.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class MainTest {

    private static Thread TESTED;
    private final static int PORT = 8080;
    private final static File userDir = new File(System.getProperty("user.dir")),
            testBaseDir = new File(userDir, "src/test/resources");
    private static final String[] args = {"-p", "" + PORT, "-d", testBaseDir.getAbsolutePath()};
    private static int totalRequests = 0;
    private static double totalTime = 0;
    private final List<Exception> exceptions = new LinkedList<Exception>();

    /**
     * Runs the Main.main in its own Thread.
     */
    @BeforeClass
    public static void setUpClass() {
        TESTED = new Thread(new Runnable() {
            public void run() {
                Main.main(args);
            }
        });
        TESTED.start();
    }

    /**
     * Stops the Main.main running in its own Thread and prints the summary
     * tests results.
     *
     * @throws InterruptedException when something happen during the Thread
     * interrupt
     */
    @AfterClass
    public static void tearDownClass() throws InterruptedException {
        TESTED.interrupt();
        TESTED.join();
        TESTED = null;

        System.out.println("---");
        System.out.printf("Total %d requests have been served in %.3f s.\n", totalRequests, totalTime / 1000);
        System.out.printf("To serve one request tooks %.3f ms in average.\n", totalTime / totalRequests);
        System.out.println("---");
    }

    /**
     * Ensure the webserver is running.
     *
     * @throws InterruptedException
     */
    @Before
    public synchronized void setUp() throws InterruptedException {
        wait(1000);
    }

    /**
     * Clear the list of exceptions.
     *
     * @throws InterruptedException
     */
    @After
    public synchronized void tearDown() throws InterruptedException {
        exceptions.clear();
        wait(1000);
    }

    /**
     * Test with 100 clients.
     *
     * @throws InterruptedException
     */
    @Test
    public void loadTest100() throws InterruptedException {
        loadTest(100);
    }

//    /**
//     * Test with 1000 clients.
//     *
//     * @throws InterruptedException //
//     */
//    @Test
//    public void loadTest1000() throws InterruptedException {
//        loadTest(1000);
//    }
//
//    /**
//     * Test with 2000 clients.
//     *
//     * @throws InterruptedException
//     */
//    @Test
//    public void leodTest2000() throws InterruptedException {
//        for (int i = 0; i < 2; i++) {
//            loadTest1000();
//        }
//    }
    /**
     * Test with the given number of clients and prints the test results.
     *
     * @param numOfClients number of clients to test the server
     * @throws InterruptedException
     */
    private void loadTest(int numOfClients) throws InterruptedException {
        Thread[] clients = new Thread[numOfClients];
        for (int i = 0; i < numOfClients; i++) {
            clients[i] = new Thread(new ClientRunnable("index.html"));
        }

        long start = System.currentTimeMillis();
        for (Thread c : clients) {
            c.start();
        }
        for (Thread c : clients) {
            c.join();
        }
        long stop = System.currentTimeMillis();

        assertEquals("Somthing gone wrong during the test.", 0, exceptions.size());
        double time = ((double) stop - start);
        double avgTime = time / numOfClients;
        totalRequests += numOfClients;
        totalTime += time;

        System.out.println("---");
        System.out.printf("%d requests have been served in %.3f s.\n", numOfClients, time / 1000);
        System.out.printf("To serve one request tooks %.3f ms in average.\n", avgTime);
        System.out.println("---");
    }

    private class ClientRunnable implements Runnable {

        private final String url;

        public ClientRunnable(String fileName) {
            this.url = "http://localhost:" + PORT + "/" + fileName;
        }

        public void run() {
            try {
                JEditorPane jep = new JEditorPane();
                jep.setPage(new URL(this.url));
            } catch (IOException ex) {
                exceptions.add(ex);
            }
        }
    }
}
