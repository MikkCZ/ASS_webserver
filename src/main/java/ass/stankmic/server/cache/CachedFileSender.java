package ass.stankmic.server.cache;

import ass.stankmic.main.CanonicalFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles sending the files to the client including caching the files on the
 * server side.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class CachedFileSender {

    /**
     * Maximum time how long the file should stay in the cache when not
     * accessed.
     */
    private static final long MAX_CACHE_AGE = 60000L; // 1minute = 60 000 ms
    private static final int INITIAL_CACHE_SIZE = 128;
    private final Map<String, SoftReference<CachedFile>> cached;

    private CachedFileSender() {
        cached = new ConcurrentHashMap<String, SoftReference<CachedFile>>(INITIAL_CACHE_SIZE);
        initCleaningThread();
    }

    private void initCleaningThread() {
        final Thread cleaning = new Thread(new CacheCleaningRunnable());
        cleaning.start();
    }

    /**
     * Send the file to the given output stream/writer.
     *
     * @param file
     * @param outWriter
     * @param outStream
     * @throws FileNotFoundException
     * @throws IOException when cannot load the file
     */
    public void sendFile(final File file, final PrintWriter outWriter, final OutputStream outStream) throws FileNotFoundException, IOException {
        final String filePath = CanonicalFile.get(file).getPath();
        CachedFile toSend;

        final SoftReference<CachedFile> sr = cached.get(filePath);
        if (sr != null) {
            toSend = sr.get();
        } else {
            toSend = cacheFile(filePath, file);
        }
        if (toSend == null) {
            cached.remove(filePath);
            toSend = cacheFile(filePath, file);
        }
        toSend.sendFile(outWriter, outStream);
    }

    /**
     * Cache the file when not present in the cache.
     *
     * @param filePath
     * @param file
     * @return CachedFile instace, which has been stored in the cache
     * @throws FileNotFoundException
     * @throws IOException
     */
    private CachedFile cacheFile(final String filePath, final File file) throws FileNotFoundException, IOException {
        CachedFile newCachedFile = CachedFileFactory.newInstance(file);
        cached.put(filePath, new SoftReference<CachedFile>(newCachedFile));
        return newCachedFile;
    }

    public static CachedFileSender getInstance() {
        return FileCacheHolder.INSTANCE;
    }

    private static class FileCacheHolder {

        private static final CachedFileSender INSTANCE = new CachedFileSender();
    }

    /**
     * Cleaning Thread, which cleans obsolete and unused files from the cache.
     */
    private class CacheCleaningRunnable implements Runnable {

        public synchronized void run() {
            while (true) {
                for (String filePath : cached.keySet()) {
                    final CachedFile cf = cached.get(filePath).get();
                    if (cf == null) {
                        cached.remove(filePath);
                    } else if (!cf.isUpToDate() || (System.currentTimeMillis() - cf.getLastUsage()) > MAX_CACHE_AGE) {
                        cached.remove(filePath);
                    }
                }

                try {
                    if (cached.isEmpty()) {
                        wait(MAX_CACHE_AGE);
                    }
                    wait(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CachedFileSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
