package ass.stankmic.server.requests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class POSTRequestHandler implements RequestHandler {

	protected POSTRequestHandler() {
	}

	public void serveTheRequest(Request request, OutputStream outStream, PrintWriter outWriter) throws BadHTTPRequestException {
        final InputStream is = request.getInputStream();
        if (is == null) {
            HTTPCode.code500.sendResponse(outWriter);
            return;
        }
        try {
            count(request.getContentLength(), is);
        } catch (IOException ex) {
            HTTPCode.code500.sendResponse(outWriter);
            System.out.println("POST count error, returned 500");
            ex.printStackTrace();
            return;
        }
        HTTPCode.code204.sendResponse(outWriter);
    }

	private void count(final int length, final InputStream is) throws IOException {
		final byte[] buffer = new byte[length];
		IOUtils.readFully(is, buffer, 0, length);
		try(ByteArrayInputStream baos = new ByteArrayInputStream(buffer)) {
			try(GZIPInputStream gis = new GZIPInputStream(baos)) {
				try(BufferedReader br = new BufferedReader(new InputStreamReader(gis, Charset.forName("UTF-8")))) {
					final StringBuilder sb = new StringBuilder();
					String line;
			        while((line = br.readLine()) != null) {
			        	sb.append(line);
			        }
					WordCounter.getInstance().addWords(sb.toString());
				}
			}
		}
	}

}