package ass.stankmic.server.requests.exceptions;

/**
 * Use to signalize unimplemented or unknown HTTP request (501 Not Implemented).
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class NotImplementedHTTPMethodException extends Exception {

	private static final long serialVersionUID = 7292426540037220616L;

	/**
     * Creates a new instance of
     * <code>NotSupportedMethodException</code> without detail message.
     */
    public NotImplementedHTTPMethodException() {
    }

    /**
     * Constructs an instance of
     * <code>NotSupportedMethodException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public NotImplementedHTTPMethodException(String msg) {
        super(msg);
    }
}
