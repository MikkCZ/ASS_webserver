package ass.stankmic.server.requests;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.NotImplementedHTTPMethodException;

/**
 * Factory to get the proper RequestHandler instance to handle the desired
 * request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class RequestHandlerFactory {

    private RequestHandlerFactory() {
    }

    /**
     * Returns an instance implementing RequestHandler interface according to
     * the given HTTP request.
     *
     * @param request
     * @return RequestHandler for the given HTTP request
     * @throws BadHTTPRequestException when the request in bad formed (has not 3
     * parts)
     * @throws NotImplementedHTTPMethodException when the request is not
     * supported
     */
    public static RequestHandler newInstance(final Request request) throws BadHTTPRequestException, NotImplementedHTTPMethodException {
        final RequestMethod rm = RequestMethod.recognizeMethod(request.getMethodString());
        switch (rm) {
            case GET:
                return new GETRequestHandler();
            case POST:
                return new POSTRequestHandler();
            default:
                throw new NotImplementedHTTPMethodException();
        }
    }

    /**
     * RequestMethod enum is used to identify the request method easily.
     */
    private enum RequestMethod {

        GET,
        POST;

        /**
         * Returns enum value according to the given method string.
         *
         * @param methodString
         * @return enum according to the given method
         * @throws NotImplementedHTTPMethodException when the method is not
         * recognized or not implemented
         */
        protected static RequestMethod recognizeMethod(String methodString) throws NotImplementedHTTPMethodException {
            methodString = methodString.toUpperCase();
            if ("GET".equals(methodString)) {
                return GET;
            } else if ("POST".equals(methodString)) {
                return POST;
            } else {
                throw new NotImplementedHTTPMethodException();
            }
        }
    }
}
