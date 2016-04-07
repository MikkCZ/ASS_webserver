package ass.stankmic.main;

import ass.stankmic.structures.WorkerPoolImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ProxyRunnable implements Runnable {

    private final int port1, port2;
    private final ServerSocket serverSoc;

    protected ProxyRunnable(int port, int port1, int port2) throws SocketException {
        try {
            this.serverSoc = new ServerSocket(port);
        } catch (IOException ex) {
            throw new SocketException("Cannot open socket using port " + port + " - may be already in use by another program.");
        }
        this.port1 = port1;
        this.port2 = port2;
    }

    public void run() {
        Socket remoteSoc;
        try {
            while ((remoteSoc = serverSoc.accept()) != null) {
                startNewClientThread(remoteSoc, port1, port2);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProxyRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startNewClientThread(Socket remoteSoc, int port1, int port2) {
        try {
            WorkerPoolImpl.getInstance().run(new ProxyClientRunnable(remoteSoc, port1, port2));
        } catch (IOException ex) {
            Logger.getLogger(ProxyRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
