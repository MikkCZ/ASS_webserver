package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Implementation of this interface is used to handle one specific method from
 * the HTTP request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public interface RequestHandler {

    /**
     * Serves the request given in a List of its lines.
     *
     * @param request already loaded from the socket
     * @param baseDir base directory the webserver uses
     * @param outStream output stream to send the response
     * @param outWriter output writer to send the response
     * @throws BadHTTPRequestException when the request is determined as not
     * formed properly
     */
    public void serveTheRequest(final Request request, final File baseDir, final OutputStream outStream, final PrintWriter outWriter) throws BadHTTPRequestException;
}
