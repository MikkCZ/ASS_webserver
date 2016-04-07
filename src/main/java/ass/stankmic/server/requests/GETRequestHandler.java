package ass.stankmic.server.requests;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;

/**
 * Implements RequestHandler interface to handle the GET request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class GETRequestHandler implements RequestHandler {

    private static final Pattern REQUEST_PATERN = Pattern.compile("GET (\\S){1,} HTTP/1\\.[0,1]");
    private static final String DEFAULT_DIR_INDEX = "index.html";

    protected GETRequestHandler() {
    }

    /**
     * Serves the given Request for the client on the other side of given
     * PrintWriter and OutputStream.
     *
     * @param request
     * @param baseDir base directory the server uses
     * @param outStream
     * @param outWriter
     * @throws BadHTTPRequestException when the Request is not formed well
     */
    public void serveTheRequest(final Request request, final OutputStream outStream, final PrintWriter outWriter) throws BadHTTPRequestException {
        request.setMethodPattern(REQUEST_PATERN);
        String path = request.getPath();
        if (path == null || "".equals(path)) {
            path = DEFAULT_DIR_INDEX;
        } else if (path.endsWith("/")) {
            path += DEFAULT_DIR_INDEX;
        }

        // get the requested File instance with canonical/abosolute path
        /*final File requested = null;

        try {
            fileSender.sendFile(requested, outWriter, outStream);
        } catch (FileNotFoundException ex) {
            HTTPCode.code404.sendResponse(outWriter);
        } catch (IOException ex) {
            HTTPCode.code500.sendResponse(outWriter);
            System.err.printf("Unexpected error when sending file %s.\n", path);
        }*/
    }
}
