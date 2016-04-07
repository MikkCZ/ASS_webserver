package ass.stankmic.server.cache;

import ass.stankmic.server.requests.HTTPCode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use to store text file in cache.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class CachedTextFile extends CachedFile {

    private final String fileString;

    /**
     * Constructor - handles also loading of the file, but caches only file
     * smaller than MAX_CACHED_FILE_SIZE.
     *
     * @param file binary file to store
     * @param contentType of the file
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    protected CachedTextFile(final File file, final String contentType) throws FileNotFoundException, IOException {
        super(file, contentType);
        final long fileSize = file.length();
        if (fileSize > MAX_CACHED_FILE_SIZE) {
            this.fileString = null;
        } else {
            this.fileString = load(file);
        }
    }

    /**
     * Handles loading of the file.
     *
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    private String load(final File file) throws FileNotFoundException, IOException {
        final StringBuilder sb = new StringBuilder((int) file.length());
        final InputStream is = new FileInputStream(file);
        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        is.close();
        return sb.toString();
    }

    @Override
    protected void sendFile(final PrintWriter outWriter, final OutputStream outStream) {
        if (fileString != null) {
            HTTPCode.code200.sendResponse200(outWriter, contentType);
            outWriter.println(fileString);
        } else {
            try {
                final FileInputStream fis = new FileInputStream(file);
                HTTPCode.code200.sendResponse200(outWriter, contentType);
                sendFileDirectly(fis, outWriter);
                fis.close();
            } catch (FileNotFoundException ex) {
                HTTPCode.code404.sendResponse(outWriter);
            } catch (IOException ex) {
                Logger.getLogger(CachedTextFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        outWriter.flush();
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Use to send file directly from the disk, when not cached due to its size.
     *
     * @param fis FileInputStream of the file
     * @param outWriter PrintWriter where to send the file content
     */
    private void sendFileDirectly(final FileInputStream fis, final PrintWriter outWriter) {
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                outWriter.println(line);
            }
            br.close();
        } catch (IOException ex) {
            System.err.println("Error when sending text cached file.");
        }
    }
}
