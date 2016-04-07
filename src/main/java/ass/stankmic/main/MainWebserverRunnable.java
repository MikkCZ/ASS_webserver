package ass.stankmic.main;

import ass.stankmic.structures.WorkerPoolImpl;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main webserver runnable, which handles the routine of accepting and
 * distributing incoming connections.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class MainWebserverRunnable implements Runnable {

    private final File baseDir;
    private final int port;
    private final ServerSocket serverSoc;

    /**
     * MainWebserverRunnable contructor.
     *
     * @param port port where the webserver should open a socket, range should
     * be checked before
     * @param baseDir base directory the webserver should use, its existance and
     * if is directory should be checked before
     * @throws SocketException in case of socket creation problem (e.g. port in
     * use)
     */
    protected MainWebserverRunnable(final int port, final File baseDir) throws SocketException {
        // check base directory
        this.baseDir = baseDir;

        // create socket on the given port
        this.port = port;
        try {
            this.serverSoc = new ServerSocket(port);
        } catch (IOException ex) {
            throw new SocketException("Cannot open socket using port " + port + " - may be already in use by another program.");
        }
    }

    /**
     * Handles the MainWebserverRunnable routine of accepting and distributing
     * incoming connections.
     */
    public void run() {
        System.out.printf("Webserver started on port %d.\n", port);
        System.out.printf("Base directory is: %s\n", baseDir.getAbsolutePath());

        Socket remoteSoc;
        try {
            while ((remoteSoc = serverSoc.accept()) != null) {
                startNewClientThread(remoteSoc);
            }
        } catch (IOException ex) {
            releaseResources();
        }
    }

    /**
     * Add new ClientWebserverRunnable to server the request from given Socket
     * into the WoekerPool.
     *
     * @param remoteSoc Socket with the client connection
     */
    private void startNewClientThread(final Socket remoteSoc) {
        try {
            WorkerPoolImpl.getInstance().run(new ClientWebserverRunnable(remoteSoc, baseDir));
        } catch (IOException ex) {
            Logger.getLogger(MainWebserverRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Releases the class sources - shouldn't be used before the thread is
     * terminated or the run() method has still something to do.
     */
    protected void releaseResources() {
        try {
            serverSoc.close();
        } catch (IOException ex) {
            System.err.printf("The server socket couldn't be closed. The port %d may stay unavailable until your computer restart.\n", port);
        }
    }
}
