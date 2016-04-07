package ass.stankmic.server.cache;

import ass.stankmic.main.ClientWebserverRunnable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use to get the right extension of the CachedFile according to the file
 * Content-Type.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class CachedFileFactory {

    /**
     * Default Content-Type which will be used for any unrecognized files.
     */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private CachedFileFactory() {
    }

    /**
     * Get the right extension of the CachedFile according to the file
     * Content-Type.
     *
     * @param file
     * @return instance of the CachedFile
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    protected static CachedFile newInstance(final File file) throws FileNotFoundException, IOException {
        final String contentType = getContentType(file);
        if (contentType.startsWith("text")) {
            return new CachedTextFile(file, contentType);
        } else {
            return new CachedBinaryFile(file, contentType);
        }
    }

    /**
     * Try to guess the Content-Type of the requested file according its content
     * or file extension.
     *
     * @param file to recognize
     * @return Content-Type as a string or default content type
     * (DEFAULT_CONTENT_TYPE) when not recognized
     */
    private static String getContentType(final File file) {
        String contentType = null;
        try {
            final InputStream is = new FileInputStream(file);
            contentType = URLConnection.guessContentTypeFromStream(is);
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientWebserverRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (contentType == null) {
            try {
                contentType = Files.probeContentType(file.toPath());
            } catch (IOException ex) {
                Logger.getLogger(ClientWebserverRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return contentType;
    }
}
