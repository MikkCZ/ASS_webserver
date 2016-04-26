package ass.stankmic.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import ass.stankmic.server.requests.HTTPCode;
import ass.stankmic.server.requests.Request;
import ass.stankmic.server.requests.RequestHandler;
import ass.stankmic.server.requests.RequestHandlerFactory;
import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import ass.stankmic.server.requests.exceptions.NotImplementedHTTPMethodException;

/**
 * Client runnable to handle one client HTTP request.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ClientWebserverRunnable implements Runnable {

    private final Socket remoteSoc;
    private final OutputStream outStream;
    private final PrintWriter outWriter;

    /**
     * ClientWebserverRunnable contructor
     *
     * @param remoteSoc connected client socket
     * @param baseDir base directory the webserver uses
     * @throws java.io.IOException when opening the output stream failes
     */
    protected ClientWebserverRunnable(final Socket remoteSoc) throws IOException {
        this.remoteSoc = remoteSoc;
        this.outStream = remoteSoc.getOutputStream();
        this.outWriter = new PrintWriter(outStream);
    }

    /**
     * Handles the clients HTTP request.
     */
    public void run() {
        try {
            // client connected - get the request
            final Request request = loadRequest();
            // get the request handler according the request method
            if (request.isEmpty()) {
                throw new BadHTTPRequestException();
            }
            final RequestHandler rh = RequestHandlerFactory.newInstance(request);
            rh.serveTheRequest(request, outStream, outWriter);
        } catch (IOException ex) {
            HTTPCode.code500.sendResponse(outWriter);
        } catch (BadHTTPRequestException ex) {
            // bad request
            HTTPCode.code400.sendResponse(outWriter);
        } catch (NotImplementedHTTPMethodException ex) {
            // not implmented method
            HTTPCode.code501.sendResponse(outWriter);
        } finally {
            releaseResources();
        }
    }

    /**
     * Loads the request from remoteSoc.
     *
     * @return request line by line in a list
     * @throws IOException
     */
    private Request loadRequest() throws IOException {
    	final InputStream is = remoteSoc.getInputStream();
        final Request request = new Request();
        
        final byte[] readBytes = new byte[1];
        String nowRead;
        final String[] prevRead = new String[2];
        
        final StringBuilder sb = new StringBuilder();
        while(true) {
        	is.read(readBytes);
        	nowRead = new String(readBytes);
        	if("\n".equals(nowRead)) {
        		if("\n".equals(prevRead[1])) {
        			break;
        		}
        		final String line = sb.toString();
        		request.addLine(line.substring(0, line.length()-1));
        		sb.setLength(0);
        	} else {
        		sb.append(nowRead);
        	}
        	prevRead[1] = prevRead[0];
        	prevRead[0] = nowRead;
        }
        
        request.freeze();
        request.setInputStream(is);
        return request;
    }

    /**
     * Releases the class sources - shouldn't be used before the thread is
     * terminated or the run() method has still something to do.
     */
    private void releaseResources() {
        // flush and close all resources
        outWriter.flush();
        outWriter.close();
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException ex) {
            System.err.println("The output stream couldn't be closed.");
        }
        try {
            remoteSoc.close();
        } catch (IOException ex) {
            System.err.println("The remote socket couldn't be closed.");
        }
    }
}
