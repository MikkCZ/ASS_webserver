package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.NotImplementedHTTPMethodException;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test the RequestHandlerFactory.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class RequestHandlerFactoryTest {

    private final String HTTPver = "HTTP/1.1";

    /**
     * Test newInstance method for GET request.
     */
    @Test
    public void testGETrequest() {
        Request request = new Request();
        request.addLine("GET path " + HTTPver);
        request.freeze();
        RequestHandler handler = null;
        try {
            handler = RequestHandlerFactory.newInstance(request);
        } catch (BadHTTPRequestException ex) {
            assertTrue("The factory hadn't parsed the request well.", false);
        } catch (NotImplementedHTTPMethodException ex) {
            assertTrue("The factory recognized the GET request as not implemented.", false);
        }
        assertTrue("Incorrect request handler returned.", handler instanceof GETRequestHandler);
    }

    /**
     * Test newInstance method for non GET request.
     *
     * @throws NotImplementedHTTPMethodException expected
     */
    @Test(expected = NotImplementedHTTPMethodException.class)
    public void testNotGETrequest() throws NotImplementedHTTPMethodException {
        Request request = new Request();
        request.addLine("POST path " + HTTPver);
        request.freeze();
        try {
            RequestHandlerFactory.newInstance(request);
        } catch (BadHTTPRequestException ex) {
            assertTrue("The factory hadn't parsed the request well.", false);
        }
    }

    /**
     * Test newInstace method for invalid HTTP request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testBadHTTPRequest() throws BadHTTPRequestException {
        Request request = new Request();
        request.addLine("POST path");
        request.freeze();
        try {
            RequestHandlerFactory.newInstance(request);
        } catch (NotImplementedHTTPMethodException ex) {
            assertTrue("The factory hadn't parsed the request well.", false);
        }
    }

    /**
     * Test newInstace method for invalid HTTP GET request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testBadHTTPGETRequest() throws BadHTTPRequestException {
        Request request = new Request();
        request.addLine("GET path");
        request.freeze();
        try {
            RequestHandlerFactory.newInstance(request);
        } catch (NotImplementedHTTPMethodException ex) {
            assertTrue("The factory hadn't parsed the request well.", false);
        }
    }

    /**
     * Test newInstace method for invalid HTTP GET request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testBadHTTPGETRequest2() throws BadHTTPRequestException {
        Request request = new Request();
        request.addLine("GET  path");
        request.freeze();
        try {
            RequestHandlerFactory.newInstance(request);
        } catch (NotImplementedHTTPMethodException ex) {
            assertTrue("The factory hadn't parsed the request well.", false);
        }
    }
}
