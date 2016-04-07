package ass.stankmic.server.cache;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Extensions of this class are used to store files in the cache.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public abstract class CachedFile {

    /**
     * Maximum size of the file which can be stored in the cache, large file
     * have to still behave like cached on should be send directly from the disk
     * when the calling sendFile() method.
     */
    public static final int MAX_CACHED_FILE_SIZE = Integer.MAX_VALUE;
    protected final File file;
    protected final String contentType;
    private final long cachedTime;
    protected long lastUsed;

    /**
     * Default contructor to save the given File and its Content-Type.
     *
     * @param file
     * @param contentType
     */
    protected CachedFile(final File file, final String contentType) {
        this.file = file;
        this.contentType = contentType;
        this.cachedTime = System.currentTimeMillis();
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Use to send the file.
     *
     * @param outWriter
     * @param outStream
     */
    protected abstract void sendFile(final PrintWriter outWriter, final OutputStream outStream);

    /**
     * Check the cached version of the file is still up to date.
     *
     * @return false if the file on disk has changed from the time when it has
     * been cached
     */
    protected final boolean isUpToDate() {
        return (cachedTime > file.lastModified());
    }

    /**
     * Get time when the file has been last accessed inthe cache, or cached,
     * when never accessed.
     *
     * @return time of the last access
     */
    protected final long getLastUsage() {
        return lastUsed;
    }
}
