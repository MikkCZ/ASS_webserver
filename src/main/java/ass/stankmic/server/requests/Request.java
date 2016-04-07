package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use to store a HTTP request and make some basic operations about it.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class Request {

    private static final String AUTH_LINE_BEGIN = "Authorization: Basic ",
            CONTENT_LENGTH_LINE_BEGIN = "Content-Length: ";
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("(\\S){1,} (\\S){1,} HTTP/1\\.[0,1]"),
            AUTH_LINE_PATTERN = Pattern.compile(AUTH_LINE_BEGIN + "(\\S)*"),
            CONTENT_LENGTH_LINE_PATTERN = Pattern.compile(CONTENT_LENGTH_LINE_BEGIN + "[0-9]{1,}");
    private Pattern methodPattern = DEFAULT_PATTERN;
    private final List<String> requestLines;
    private boolean frozen;
    private InputStream socketIs;

    /**
     * Contructor.
     */
    public Request() {
        this.requestLines = new ArrayList<String>(10);
        frozen = false;
    }

    /**
     * Add next line of the request and freezes it when the line is empty.
     *
     * @param line
     * @return true if succesfully addes
     */
    public boolean addLine(final String line) {
        if (!frozen) {
            if (line == null || "".equals(line)) {
                freeze();
                return false;
            }
            return requestLines.add(line);
        }
        return false;
    }

    public void setInputStream(InputStream is) {
        socketIs = is;
    }

    /**
     * Freezes completed request.
     */
    public void freeze() {
        frozen = true;
    }

    /**
     * Check there is somthing in the request.
     *
     * @return true if the Request is empty
     */
    public boolean isEmpty() {
        return requestLines.isEmpty();
    }

    /**
     * Sets the method Pattern which can be used for the request validity
     * afterwards.
     *
     * @param methodPattern
     */
    protected void setMethodPattern(final Pattern methodPattern) {
        this.methodPattern = methodPattern;
    }

    /**
     * Checks the request is valid according the the Pattern given by
     * setMethodPattern() before, or some default Pattern.
     *
     * @return true if the request is valid
     */
    private boolean isValid() {
        if (!isEmpty()) {
            final Matcher m = methodPattern.matcher(requestLines.get(0));
            return m.matches();
        }
        return false;
    }

    /**
     * Returns the HTTP method as a String, e.g. GET.
     *
     * @return HTTP method as a String
     * @throws BadHTTPRequestException when the request is not valid
     */
    protected String getMethodString() throws BadHTTPRequestException {
        if (!isValid()) {
            throw new BadHTTPRequestException();
        }
        return requestLines.get(0).split(" ")[0];
    }

    /**
     * Returns path of the request.
     *
     * @return path of the request as a String
     * @throws BadHTTPRequestException when the request is not valid
     */
    public String getPath() throws BadHTTPRequestException {
        if (!isValid()) {
            throw new BadHTTPRequestException();
        }
        return requestLines.get(0).split(" ")[1];
    }

    /**
     * Returns base64 encoded Basic authorization credentials.
     *
     * @return base64 encoded basic authorization credentials, null if not in
     * the request
     */
    public String getBasicAuthorization() {
        String authLine = null;
        Matcher m;
        for (String line : requestLines) {
            m = AUTH_LINE_PATTERN.matcher(line);
            if (m.matches()) {
                authLine = line;
                break;
            }
        }
        if (authLine == null) {
            return null;
        } else {
            return authLine.split(" ")[2];
        }
    }

    public int getContentLength() {
        String contentLengthLine = null;
        Matcher m;
        for (String line : requestLines) {
            m = CONTENT_LENGTH_LINE_PATTERN.matcher(line);
            if (m.matches()) {
                contentLengthLine = line;
                break;
            }
        }
        if (contentLengthLine == null) {
            return 0;
        } else {
            return Integer.parseInt(contentLengthLine.split(" ")[1]);
        }
    }

    public InputStream getInputStream() {
        return socketIs;
    }

    public List<String> getWhole() {
        return requestLines;
    }
}
