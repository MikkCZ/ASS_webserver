package ass.stankmic.main;

import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ProxyMain {

    private static final int port = 8080,
            port1 = 8081,
            port2 = 8082;
//    private static final File baseDir = Main.DEFAULT_BASE_DIR;
    private static Runnable r1, r2, mainRunnable;
    private static Thread t1, t2, mainThread;
//    private static final String p1 = "hello";
//    private static final String p2 = "hello2";

    public static void main(String[] args) {
        try {
//            r1 = new MainWebserverRunnable(port1, new File(baseDir, p1));
//            r2 = new MainWebserverRunnable(port2, new File(baseDir, p2));
            mainRunnable = new ProxyRunnable(port, port1, port2);
        } catch (SocketException ex) {
            Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
//        t1 = new Thread(r1);
//        t2 = new Thread(r2);
        mainThread = new Thread(mainRunnable);
//        t1.start();
//        t2.start();
        mainThread.start();
    }

}
