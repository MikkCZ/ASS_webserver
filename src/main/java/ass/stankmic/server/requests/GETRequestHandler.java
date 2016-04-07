package ass.stankmic.server.requests;

import ass.stankmic.main.CanonicalFile;
import ass.stankmic.server.cache.CachedFileSender;
import ass.stankmic.server.requests.auth.Auth;
import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.RequestToAccessFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * Implements RequestHandler interface to handle the GET request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class GETRequestHandler implements RequestHandler {

    private static final Pattern REQUEST_PATERN = Pattern.compile("GET (\\S){1,} HTTP/1\\.[0,1]");
    private static final CachedFileSender fileSender = CachedFileSender.getInstance();
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
    public void serveTheRequest(final Request request, final File baseDir, final OutputStream outStream, final PrintWriter outWriter) throws BadHTTPRequestException {
        request.setMethodPattern(REQUEST_PATERN);
        String path = request.getPath();
        if (path == null || "".equals(path)) {
            path = DEFAULT_DIR_INDEX;
        } else if (path.endsWith("/")) {
            path += DEFAULT_DIR_INDEX;
        }

        // get the requested File instance with canonical/abosolute path
        final File requested = CanonicalFile.get(new File(baseDir, path));

        // check the File exists (and is not a directory)
        // check the File is in base directory scope
        if (!requested.exists()) {
            HTTPCode.code404.sendResponse(outWriter);
            return;
        } else if (!isInBaseDirScope(baseDir, requested)) {
            HTTPCode.code403.sendResponse(outWriter);
            return;
        }
        try {
            if (!Auth.isAuthorized(request, baseDir)) {
                HTTPCode.code401.sendResponse401(outWriter);
                return;
            }
        } catch (RequestToAccessFileException ex) {
            HTTPCode.code403.sendResponse(outWriter);
            return;
        }
        try {
            fileSender.sendFile(requested, outWriter, outStream);
        } catch (FileNotFoundException ex) {
            HTTPCode.code404.sendResponse(outWriter);
        } catch (IOException ex) {
            HTTPCode.code500.sendResponse(outWriter);
            System.err.printf("Unexpected error when sending file %s.\n", path);
        }
    }

    /**
     * Checks the requested file is in the base directory scope.
     *
     * @param baseDir base directory the server uses
     * @param requested file
     * @return true if the file is in any (sub) directory of the base directory
     * or the base directory itself
     */
    private boolean isInBaseDirScope(final File baseDir, final File requested) {
        if (requested.exists()) {
            File tmp = requested;
            while (tmp != null) {
                if (tmp.equals(baseDir)) {
                    return true;
                }
                tmp = tmp.getParentFile();
            }
        }
        return false;
    }
}
