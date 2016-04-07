package ass.stankmic.server.requests.exceptions;

/**
 * Indicated request for file containing Basic authentification credentials (403
 * Forbidden should follow).
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class RequestToAccessFileException extends Exception {

    /**
     * Creates a new instance of <code>RequestToAccessFileException</code>
     * without detail message.
     */
    public RequestToAccessFileException() {
    }

    /**
     * Constructs an instance of <code>RequestToAccessFileException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public RequestToAccessFileException(String msg) {
        super(msg);
    }
}
