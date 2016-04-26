package ass.stankmic.server.requests;

import java.io.OutputStream;
import java.io.PrintWriter;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;

/**
 * Implements RequestHandler interface to handle the GET request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class GETRequestHandler implements RequestHandler {

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
    	HTTPCode.code200.sendResponse200(outWriter, "text/plain");
    	outWriter.println(WordCounter.getInstance().getCount());
    	System.out.println("GET ok");
    }
}
