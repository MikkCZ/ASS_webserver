package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import javax.xml.bind.DatatypeConverter;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the Request class behavior.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class RequestTest {

    private final String HTTPver = "HTTP/1.1";
    private Request TESTED;

    @Before
    public void setUp() {
        TESTED = new Request();
    }

    @After
    public void tearDown() {
        TESTED = null;
    }

    /**
     * Tests adding new line and isEmpty before and afterwards.
     */
    @Test
    public void testAddLine() {
        assertTrue("New Request is not empty.", TESTED.isEmpty());
        assertTrue("New Request does not accept new lines.", TESTED.addLine("line"));
        assertFalse("Request is not empty after adding a line.", TESTED.isEmpty());
    }

    /**
     * Tests adding blank line and request freeze afterwards.
     */
    @Test
    public void testAddBlankLine() {
        assertFalse("Request accepts blank lines.", TESTED.addLine(""));
        assertTrue("Request is not empty after adding a blank line.", TESTED.isEmpty());
        assertFalse("Request accepts more lines after blank line.", TESTED.addLine("line"));
    }

    /**
     * Tests adding null line and request freeze afterwards.
     */
    @Test
    public void testAddNullLine() {
        assertFalse("Request accepts null lines.", TESTED.addLine(null));
        assertTrue("Request is not empty after adding a null line.", TESTED.isEmpty());
        assertFalse("Request accepts more lines after null line.", TESTED.addLine("line"));
    }

    /**
     * Test freezing the request.
     */
    @Test
    public void testFreeze() {
        TESTED.freeze();
        assertFalse("Frozen request accepts new lines.", TESTED.addLine("line"));
    }

    /**
     * Test getting method string.
     *
     * @throws BadHTTPRequestException
     */
    @Test
    public void testGetMethodString() throws BadHTTPRequestException {
        String methodString = "GET";
        TESTED.addLine(methodString + " path " + HTTPver);
        assertEquals("The returned method String does not corresond.", methodString, TESTED.getMethodString());
    }

    /**
     * Test getting method string from bad request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testGetMethodStringForBadRequest() throws BadHTTPRequestException {
        TESTED.addLine("bad request");
        TESTED.getMethodString();
    }

    /**
     * Test getting method string from empty request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testGetMethodStringForEmptyRequest() throws BadHTTPRequestException {
        TESTED.getMethodString();
    }

    /**
     * Test getting path.
     *
     * @throws BadHTTPRequestException
     */
    @Test
    public void testGetPath() throws BadHTTPRequestException {
        String path = "path";
        TESTED.addLine("GET " + path + " " + HTTPver);
        assertEquals("The returned path does not corresond.", path, TESTED.getPath());
    }

    /**
     * Test getting path from bad request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testGetPathForBadRequest() throws BadHTTPRequestException {
        TESTED.addLine("bad request");
        TESTED.getPath();
    }

    /**
     * Test getting path from empty request.
     *
     * @throws BadHTTPRequestException expected
     */
    @Test(expected = BadHTTPRequestException.class)
    public void testGetPathForEmptyRequest() throws BadHTTPRequestException {
        TESTED.getPath();
    }

    /**
     * Test getting basic authentication from the request.
     */
    @Test
    public void testGetBasicAuthorization() {
        TESTED.addLine("GET path " + HTTPver);
        String credentials = DatatypeConverter.printBase64Binary("user:password".getBytes());
        TESTED.addLine("Authorization: Basic " + credentials);
        assertEquals("The returned credentials does not correspond.", credentials, TESTED.getBasicAuthorization());
    }

}
