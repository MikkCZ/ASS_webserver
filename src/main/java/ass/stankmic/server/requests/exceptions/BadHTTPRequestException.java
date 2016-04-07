package ass.stankmic.server.requests.exceptions;

/**
 * Use to signalize 400 Bad Request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class BadHTTPRequestException extends Exception {

	private static final long serialVersionUID = -2826517612972401931L;

	/**
     * Creates a new instance of <code>BadHTTPRequestException</code> without detail
     * message.
     */
    public BadHTTPRequestException() {
    }

    /**
     * Constructs an instance of <code>BadHTTPRequestException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BadHTTPRequestException(String msg) {
        super(msg);
    }
}
