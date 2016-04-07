package ass.stankmic.server.requests.auth;

import ass.stankmic.server.requests.Request;
import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.RequestToAccessFileException;
import java.io.File;
import javax.xml.bind.DatatypeConverter;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test the authorization handling class.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class AuthTest {

    private final String HTTPver = "HTTP/1.1",
            RIGHT_CREDENTIALS = "test:junit";
    private static final File userDir = new File(System.getProperty("user.dir")),
            testBaseDir = new File(userDir, "src/test/resources/");

    /**
     * Test access to with with no access restriction is authorized.
     *
     * @throws BadHTTPRequestException
     * @throws RequestToAccessFileException
     */
    @Test
    public void testIsAuthorizedFreeAccess() throws BadHTTPRequestException, RequestToAccessFileException {
        Request request = new Request();
        request.addLine("GET " + "index.html" + " " + HTTPver);
        boolean authorized = Auth.isAuthorized(request, testBaseDir);
        assertTrue("The files with free access is returned as not authorized.", authorized);
    }

    /**
     * Test access with no authorization provided to restricted file.
     *
     * @throws BadHTTPRequestException
     * @throws RequestToAccessFileException
     */
    @Test
    public void testIsAuthorized401() throws BadHTTPRequestException, RequestToAccessFileException {
        Request request = new Request();
        request.addLine("GET " + "secured/index.html" + " " + HTTPver);
        boolean authorized = Auth.isAuthorized(request, testBaseDir);
        assertFalse("The files with free access is returned as not authorized.", authorized);
    }

    /**
     * Test access with right credentials provided to restricted file.
     *
     * @throws BadHTTPRequestException
     * @throws RequestToAccessFileException
     */
    @Test
    public void testIsAuthorizedWithRightCredentials() throws BadHTTPRequestException, RequestToAccessFileException {
        Request request = new Request();
        request.addLine("GET " + "secured/index.html" + " " + HTTPver);
        String credentials = RIGHT_CREDENTIALS;
        request.addLine("Authorization: Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes()));
        boolean authorized = Auth.isAuthorized(request, testBaseDir);
        assertTrue("The files with free access is returned as not authorized.", authorized);
    }

    /**
     * Test access with wrong credentials provided to restricted file.
     *
     * @throws BadHTTPRequestException
     * @throws RequestToAccessFileException
     */
    @Test
    public void testIsAuthorizedWithWrongCredentials() throws BadHTTPRequestException, RequestToAccessFileException {
        Request request = new Request();
        request.addLine("GET " + "secured/index.html" + " " + HTTPver);
        String credentials = "wrong-user:wrong-password";
        request.addLine("Authorization: Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes()));
        boolean authorized = Auth.isAuthorized(request, testBaseDir);
        assertFalse("The files with free access is returned as not authorized.", authorized);
    }

    /**
     * Test access to access restricting file itself.
     *
     * @throws BadHTTPRequestException
     * @throws RequestToAccessFileException
     */
    @Test(expected = RequestToAccessFileException.class)
    public void testIsAuthorizedAccessFile() throws BadHTTPRequestException, RequestToAccessFileException {
        Request request = new Request();
        request.addLine("GET " + "secured/" + Auth.ACCESS_FILE_NAME + " " + HTTPver);
        Auth.isAuthorized(request, testBaseDir);
    }

}
