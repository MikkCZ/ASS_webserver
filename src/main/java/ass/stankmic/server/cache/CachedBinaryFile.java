package ass.stankmic.server.cache;

import ass.stankmic.server.requests.HTTPCode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use to store binary file in cache.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class CachedBinaryFile extends CachedFile {

    private static final int ARRAY_BUFFER_SIZE = 1024;
    private final List<byte[]> fileBytes;
    private int lastArraySize;

    /**
     * Constructor - handles also loading of the file, but caches only file
     * smaller than MAX_CACHED_FILE_SIZE.
     *
     * @param file binary file to store
     * @param contentType of the file
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    protected CachedBinaryFile(final File file, final String contentType) throws FileNotFoundException, IOException {
        super(file, contentType);
        final long fileSize = file.length();
        if (fileSize > MAX_CACHED_FILE_SIZE) {
            this.fileBytes = null;
        } else {
            this.fileBytes = new ArrayList<byte[]>(((int) file.length()) / ARRAY_BUFFER_SIZE);
            load();
        }
    }

    /**
     * Handles loading of the file.
     *
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    private void load() throws FileNotFoundException, IOException {
        if (fileBytes == null) {
            return;
        }
        fileBytes.clear();
        final InputStream is = new FileInputStream(file);
        byte[] buffer = new byte[ARRAY_BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            fileBytes.add(buffer);
            buffer = new byte[ARRAY_BUFFER_SIZE];
            lastArraySize = bytesRead;
        }
        is.close();
    }

    @Override
    protected void sendFile(final PrintWriter outWriter, final OutputStream outStream) {
        if (fileBytes != null) {
            HTTPCode.code200.sendResponse200(outWriter, contentType);
            try {
                for (int i = 0; i < fileBytes.size(); i++) {
                    if (i < fileBytes.size() - 1) {
                        outStream.write(fileBytes.get(i), 0, ARRAY_BUFFER_SIZE);
                    } else {
                        outStream.write(fileBytes.get(i), 0, lastArraySize);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error when sending binary cached file.");
            }
        } else {
            try {
                final FileInputStream fis = new FileInputStream(file);
                HTTPCode.code200.sendResponse200(outWriter, contentType);
                sendFileDirectly(fis, outStream);
                fis.close();
            } catch (FileNotFoundException ex) {
                HTTPCode.code404.sendResponse(outWriter);
            } catch (IOException ex) {
                Logger.getLogger(CachedBinaryFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Use to send file directly from the disk, when not cached due to its size.
     *
     * @param fis FileInputStream of the file
     * @param outStream OutputStream where to send the file content
     */
    private void sendFileDirectly(final FileInputStream fis, final OutputStream outStream) {
        try {
            byte[] buffer = new byte[ARRAY_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            System.err.println("Error when sending binary cached file.");
        }
    }
}
