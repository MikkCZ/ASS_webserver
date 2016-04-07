/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test GETRequestHandler class.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class GETRequestHandlerTest {

    private static final File userDir = new File(System.getProperty("user.dir")),
            testBaseDir = new File(userDir, "src/test/resources/");
    private static final String indexFileName = "index.html";
    private static final List<String> indexFile = new ArrayList<String>();
    private final String HTTPver = "HTTP/1.1";
    private RequestHandler TESTED;

    /**
     * Preload index.html file reference content.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @BeforeClass
    public static void setUpClass() throws FileNotFoundException, IOException {
        // load index.html file
        InputStream is = new FileInputStream(new File(testBaseDir, indexFileName));
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        indexFile.clear();
        String line = br.readLine();
        while (line != null) {
            indexFile.add(line);
            line = br.readLine();
        }
        br.close();
        is.close();
    }

    @Before
    public void setUp() throws FileNotFoundException, IOException {
        TESTED = new GETRequestHandler();
    }

    @After
    public void tearDown() {
        TESTED = null;
    }

    /**
     * Test of serveTheRequest method, of class GETRequestHandler.
     *
     * @throws java.io.IOException
     * @throws BadHTTPRequestException
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testCorrectGETRequest() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        request.addLine("GET " + indexFileName + " " + HTTPver);
        List<String> response = serveTheRequest(request);
        int i = 0;
        String line = response.get(i++);
        while (!"".equals(line)) {
            line = response.get(i++);
        }
        for (String expected : indexFile) {
            line = response.get(i++);
            assertEquals("The file has not been send properly.", expected, line);
        }
    }

    /**
     * Test 404 is returned for non existing file.
     *
     * @throws BadHTTPRequestException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void test404Response() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        request.addLine("GET " + "non-existing-file" + " " + HTTPver);
        List<String> response = serveTheRequest(request);
        assertTrue("404 seems to be not send for non existing file.", response.get(0).contains(HTTPCode.code404.toString()));
    }

    /**
     * Test the RequestHandler recognizes bad HTTP request (more spaces).
     *
     * @throws BadHTTPRequestException expected
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testIncorrectGETRequest1() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        // two spaces after GET
        request.addLine("GET  " + indexFileName + " " + HTTPver);
        serveTheRequest(request);
    }

    /**
     * Test the RequestHandler recognizes bad HTTP request (more spaces).
     *
     * @throws BadHTTPRequestException expected
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testIncorrectGETRequest2() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        // two spaces before HTTP protocol version
        request.addLine("GET " + indexFileName + "  " + HTTPver);
        serveTheRequest(request);
    }

    /**
     * Test the RequestHandler recognizes bad HTTP request (not GET).
     *
     * @throws BadHTTPRequestException expected
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testIncorrectGETRequest3() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        // different method
        request.addLine("POST " + indexFileName + " " + HTTPver);
        serveTheRequest(request);
    }

    /**
     * Test the RequestHandler recognizes bad HTTP request (incorrect HTTP
     * protocol version).
     *
     * @throws BadHTTPRequestException expected
     * @throws IOException
     * @throws InterruptedException
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testIncorrectGETRequest4() throws BadHTTPRequestException, IOException, InterruptedException {
        Request request = new Request();
        // incorrect HTTP protocol version
        request.addLine("GET " + indexFileName + " " + "HTTP/1.9");
        serveTheRequest(request);
    }

    /**
     * Serve the given request and return the response.
     *
     * @param request as a List of Strings
     * @return response as a List of Strings
     * @throws BadHTTPRequestException
     * @throws IOException
     * @throws InterruptedException
     */
    private synchronized List<String> serveTheRequest(Request request) throws BadHTTPRequestException, IOException, InterruptedException {
        PipedOutputStream outStream = new PipedOutputStream();
        PrintWriter outWriter = new PrintWriter(outStream);
        RequestHandlerRunnable r = new RequestHandlerRunnable(request, testBaseDir, outStream, outWriter);
        Thread handlerThread = new Thread(r);
        handlerThread.start();

        PipedInputStream is = new PipedInputStream(outStream);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        wait(100);
        if (r.ex != null) {
            throw r.ex;
        }

        List<String> response = new ArrayList<String>();
        String line = br.readLine();
        while (line != null) {
            response.add(line);
            line = br.readLine();
        }
        if (r.ex != null) {
            throw r.ex;
        }
        return response;
    }

    private class RequestHandlerRunnable implements Runnable {

        private final Request request;
        private final File baseDir;
        private final OutputStream outStream;
        private final PrintWriter outWriter;
        public BadHTTPRequestException ex;

        public RequestHandlerRunnable(Request request, File baseDir, OutputStream outStream, PrintWriter outWriter) {
            this.request = request;
            this.baseDir = baseDir;
            this.outStream = outStream;
            this.outWriter = outWriter;
        }

        public void run() {
            try {
                TESTED.serveTheRequest(request, baseDir, outStream, outWriter);
            } catch (BadHTTPRequestException e) {
                this.ex = e;
            } finally {
                outWriter.flush();
                outWriter.close();
                try {
                    outStream.flush();
                    outStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(GETRequestHandlerTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
