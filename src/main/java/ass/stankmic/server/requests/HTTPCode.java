package ass.stankmic.server.requests;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * HTTPCode enum is used to handle common HTTP responses.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public enum HTTPCode {

    code200("200 OK"),
    code201("201 Created"),
    code400("400 Bad Request"),
    code401("401 Authorization Required"),
    code403("403 Forbidden"),
    code404("404 Not Found"),
    code500("500 Internal Server Error"),
    code501("501 Not Implemented");
    private static final String HTTPver = "HTTP/1.1";
    private final String codeText;

    private HTTPCode(String codeText) {
        this.codeText = codeText;
    }

    /**
     * Sends a response header for the HTTP 200 code.
     *
     * @param outWriter output writer to send the response
     * @param contentType Content-Type of the file
     */
    public void sendResponse200(final PrintWriter outWriter, final String contentType) {
        if (this != code200) {
            throw new UnsupportedOperationException("Cannot send 200 for non-200 HTTP code.");
        }
        if (contentType == null || "".equals(contentType)) {
            throw new UnsupportedOperationException("Cannot send 200 with null or blank Content-Type.");
        }
        final Collection<String> additional = new ArrayList<String>(1);
        additional.add("Content-Type: " + contentType);
        sendResponseHeader(outWriter, additional);
    }

    /**
     * Sends a response header for the HTTP 401 code.
     *
     * @param outWriter output writer to send the response
     */
    public void sendResponse401(final PrintWriter outWriter) {
        if (this != code401) {
            throw new UnsupportedOperationException("Cannot send 401 for non-401 HTTP code.");
        }
        final Collection<String> additional = new ArrayList<String>(1);
        additional.add("WWW-Authenticate: Basic realm=\"Secure Area\"");
        sendResponseHeader(outWriter, additional);
    }

    /**
     * Sends a response (header) for the appropriate HTTP code (not 200, 401).
     *
     * @param outWriter output writer to send the response
     */
    public void sendResponse(final PrintWriter outWriter) {
        if (this == code200 || this == code401) {
            throw new UnsupportedOperationException("Specific methods should be used for 200 and 401 response.");
        }
        sendResponseHeader(outWriter, null);
    }

    /**
     * Sends a response header for the appropriate HTTP code.
     *
     * @param outWriter output writer to send the response
     * @param additional more response header lines to send (specific for the
     * HTTP code)
     * @throws UnsupportedOperationException when contentType set for other than
     * 200 code
     */
    private void sendResponseHeader(final PrintWriter outWriter, final Collection<String> additional) {
        outWriter.println(HTTPver + " " + codeText);
        if (additional != null) {
            for (String s : additional) {
                outWriter.println(s);
            }
        }
        outWriter.println("Server: stankmic");
        // blank line ends the header
        outWriter.println();
        outWriter.flush();
    }

    @Override
    public String toString() {
        return codeText;
    }
}
