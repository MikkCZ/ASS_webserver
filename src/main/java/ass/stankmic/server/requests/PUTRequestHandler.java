package ass.stankmic.server.requests;

import ass.stankmic.main.CanonicalFile;
import ass.stankmic.server.requests.exceptions.BadHTTPRequestException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class PUTRequestHandler implements RequestHandler {

    private static final Pattern REQUEST_PATERN = Pattern.compile("PUT (\\S){1,} HTTP/1\\.[0,1]");

    protected PUTRequestHandler() {
    }

    public void serveTheRequest(Request request, File baseDir, OutputStream outStream, PrintWriter outWriter) throws BadHTTPRequestException {
        request.setMethodPattern(REQUEST_PATERN);
        String path = request.getPath();
        if (path == null || "".equals(path) || path.endsWith("/")) {
            throw new BadHTTPRequestException();
        }

        final File requestedToSave = CanonicalFile.get(new File(baseDir, path));
        InputStream is = request.getInputStream();
        if (is == null) {
            HTTPCode.code500.sendResponse(outWriter);
            System.out.println("returned 500");
            return;
        }
        try {
            copy(request.getContentLength(), is, requestedToSave);
        } catch (IOException ex) {
            HTTPCode.code500.sendResponse(outWriter);
            System.out.println("returned 500");
            return;
        }
// should differ between creating a new file (201) and rewriteing the existing (200 or 204)
        HTTPCode.code201.sendResponse(outWriter);
        System.out.println("returned 201");
    }

    private void copy(int length, InputStream is, File requestedToSave) throws IOException, BadHTTPRequestException {
        File temp = File.createTempFile(Integer.toString(is.hashCode()), ".stankmic.tmp");
        FileOutputStream fos = new FileOutputStream(temp);
        byte[] buffer = new byte[1024];
        int bytesRead, totalRead = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            totalRead += bytesRead;
            if (totalRead > length) {
                fos.close();
                temp.delete();
                throw new BadHTTPRequestException();
            }
            fos.write(buffer, 0, bytesRead);
            buffer = new byte[1024];
        }
        fos.close();
        if (temp.length() == length) {
            FileInputStream fis2 = new FileInputStream(temp);
            if (!requestedToSave.exists()) {
                requestedToSave.getParentFile().mkdirs();
                requestedToSave.createNewFile();
            }
            FileOutputStream fos2 = new FileOutputStream(requestedToSave);
            buffer = new byte[1024];
            while ((bytesRead = fis2.read(buffer)) != -1) {
                fos2.write(buffer, 0, bytesRead);
                buffer = new byte[1024];
            }
            fis2.close();
            fos2.close();
            temp.delete();
        } else {
            temp.delete();
            throw new BadHTTPRequestException();
        }
    }

}
