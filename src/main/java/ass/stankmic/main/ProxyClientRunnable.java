package ass.stankmic.main;

import ass.stankmic.server.requests.HTTPCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ProxyClientRunnable implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    @SuppressWarnings("unused")
	private Socket remoteSoc, s1, s2;
    private final int port1, port2;
    private final OutputStream outStream;
    private final PrintWriter outWriter;
    private int lastRead = 0;

    public ProxyClientRunnable(Socket remoteSoc, int port1, int port2) throws IOException {
        this.remoteSoc = remoteSoc;
        this.port1 = port1;
        this.port2 = port2;
        this.outStream = remoteSoc.getOutputStream();
        this.outWriter = new PrintWriter(outStream);
    }

    public void run() {
        boolean failed = false;
        try {
            List<String> request = loadRequest();
            byte[] response1 = null, response2 = null;

            ResponseThread rt1 = null, rt2 = null;
            try {
                rt1 = new ResponseThread(port1, request);
                rt1.start();
            } catch (IOException ex) {
                response1 = null;
            }
            try {
                rt2 = new ResponseThread(port2, request);
                rt2.start();
            } catch (IOException ex) {
                response2 = null;
            }

            if (rt1 != null) {
                try {
                    rt1.join();
                    response1 = rt1.response;
                } catch (InterruptedException ex) {
                    response1 = null;
                }
            }
            if (rt2 != null) {
                try {
                    rt2.join();
                    response2 = rt2.response;
                } catch (InterruptedException ex) {
                    response2 = null;
                }
            }
            if (response1 == null && response2 == null) {
                failed = true;
            } else if (!Arrays.equals(response1, response2)) {
                if (response1 == null) {
                    outStream.write(response2);
                    return;
                } else if (response2 == null) {
                    outStream.write(response1);
                    return;
                }
                failed = true;
            }
            if (failed) {
                HTTPCode.code500.sendResponse(outWriter);
                return;
            }
            outStream.write(response1);
        } catch (IOException ex) {
            Logger.getLogger(ProxyClientRunnable.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            outWriter.flush();
            outWriter.close();
            try {
                outStream.flush();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                remoteSoc.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<String> loadRequest() throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(remoteSoc.getInputStream()));
        final List<String> request = new ArrayList<String>();
        String line = br.readLine();
        while (line != null && !"".equals(line)) {
            request.add(line);
            line = br.readLine();
        }
        request.add("");
        return request;
    }

    @SuppressWarnings("unused")
	private List<byte[]> getResponse(List<String> request, Socket s) throws IOException {
        PrintWriter pw = new PrintWriter(s.getOutputStream());
        for (String line : request) {
            pw.println(line);
        }
        pw.flush();

        InputStream is = s.getInputStream();
        List<byte[]> response = new LinkedList<byte[]>();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = is.read(buffer);
        int localLastRead = 0;

        while (bytesRead != -1) {
            response.add(buffer);
            lastRead = bytesRead;
            buffer = new byte[BUFFER_SIZE];
            bytesRead = is.read(buffer);
        }

        if (lastRead != 0) {
            lastRead = localLastRead;
        }
        return response;
    }

    private class ResponseThread extends Thread {

        Socket s;
        List<String> r;
        byte[] response = null;

        ResponseThread(int port, List<String> r) throws IOException {
            this.s = new Socket("localhost", port);
            this.r = r;
        }

        @Override
        public void run() {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter(s.getOutputStream());
                for (String line : r) {
                    pw.println(line);
                }
                pw.flush();
                InputStream is = s.getInputStream();
                response = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                pw.close();
            }
        }
    }

}
