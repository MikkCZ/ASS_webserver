package ass.stankmic.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use to start the webserver.
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class Main {

    /**
     * DEFAULT_PORT is used when not given by argument.
     */
    public static final int DEFAULT_PORT = 80;
    public static final File DEFAULT_BASE_DIR = new File(System.getProperty("user.dir"));
    private static Thread mainThread;
    private static MainWebserverRunnable mainRunnable;

    private Main() {
    }

    /**
     * Accepts two optional aguments - base directory and port, or help.
     *
     * @param args base directory (and port)
     */
    public static void main(String[] args) {
        // prepare default port and base directory values
        int port = DEFAULT_PORT;
        File baseDir = DEFAULT_BASE_DIR;

        // parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (Switch.recognizeSwitch(args[i])) {
                case HELP:
                    printHelp();
                    return;

                case PORT:
                    try {
                        int prev_port = port;
                        port = Integer.parseInt(args[i + 1]);
                        i++;
                        if (prev_port != DEFAULT_PORT) {
                            System.out.printf("Multiple port numbers specified. The last given will be used.\n");
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        System.err.printf("No port number specified after the switch.\n");
                    } catch (NumberFormatException ex) {
                        System.err.printf("Cannot parse given port number: %s\n", args[i + 1]);
                    }
                    break;

                case BASE_DIR:
                    try {
                        File prev_dir = baseDir;
                        baseDir = new File(args[i + 1]);
                        i++;
                        if (!prev_dir.equals(DEFAULT_BASE_DIR)) {
                            System.out.printf("Multiple base directories specified. The last given will be used.\n");
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        System.err.printf("No directory specified after the switch.\n");
                    }
                    break;

                default:
                    System.err.printf("Unknown argument: %s\n", args[i]);
                    break;
            }
        }

        // fix the baseDir to canonical/abosolute path
        baseDir = CanonicalFile.get(baseDir);

        // check arguments validity
        final String err = checkArguments(port, baseDir);
        if (err != null) {
            System.err.print(err);
            return;
        }
        try {
            // start the main webserver thread
            startMainWebserverThread(port, baseDir);
            createShutDownHook();
        } catch (SocketException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Prints out the txt/HELP.txt resource file.
     */
    private static void printHelp() {
        final InputStream is = ClassLoader.getSystemResourceAsStream("txt/HELP.txt");
        final InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader br = new BufferedReader(isr);
        try {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
            isr.close();
            is.close();
        } catch (IOException e) {
            System.err.println("Error reading help file.");
        }
    }

    /**
     * Check if the given arguments (port and base directory) are ok.
     *
     * @param port port where the webserver should be started
     * @param baseDir base directory the webserver should use
     * @return null if arguments are ok, error message otherwise - the webserver
     * shouldn't be started in that case
     */
    private static String checkArguments(final int port, final File baseDir) {
        String err = "";
        // check port in range
        if (port < 1 || port > 65535) {
            err += "The given port " + port + " is not in range from 1 to 65535.\n";
        }
        // check baseDir
        if (!baseDir.exists()) {
            err += "The given directory " + baseDir.toPath() + " does not exist.\n";
        } else if (!baseDir.isDirectory()) {
            err += "The given path " + baseDir.toPath() + " is not a directory.\n";
        }
        // return error message
        if (!"".equals(err)) {
            return err;
        } else {
            return null;
        }
    }

    /**
     * Starts the MainWebserverRunnable thread with given port and base
     * directory.
     *
     * @param port port where the webserver should be started
     * @param baseDir base directory the webserver should use
     */
    private static void startMainWebserverThread(final int port, final File baseDir) throws SocketException {
        mainRunnable = new MainWebserverRunnable(port, baseDir);
        mainThread = new Thread(mainRunnable);
        mainThread.start();
    }

    /**
     * Create shutdown hook for stopping th server.
     */
    private static void createShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("\n---");
                System.out.println("Server shutting down...");
                mainThread.interrupt();
                try {
                    mainRunnable.releaseResources();
                    mainThread.join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Server succesfully exited.");
            }
        }));
    }
}
