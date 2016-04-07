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

/**
 *
 * @author Michal Stanke <stankmic@fel.cvut.cz>
 */
public class ProxyClientRunnable2 implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private final Socket remoteSoc, s1, s2;
    private final OutputStream outStream;
    private final PrintWriter outWriter;
    private int lastRead = 0;

    public ProxyClientRunnable2(Socket remoteSoc, int port1, int port2) throws IOException {
        this.remoteSoc = remoteSoc;
        this.s1 = new Socket("localhost", port1);
        this.s2 = new Socket("localhost", port2);
        this.outStream = remoteSoc.getOutputStream();
        this.outWriter = new PrintWriter(outStream);
    }

    public void run() {
        boolean failed = false;
        try {
            List<String> request = loadRequest();

//            List<byte[]> response1 = getResponse(request, s1);
//            int lastRead1 = this.lastRead;
//            List<byte[]> response2 = getResponse(request, s2);
//            int lastRead2 = this.lastRead;
//            if (lastRead1 != lastRead2 || response1.size() != response2.size()) {
//                failed = true;
//            }
//            for (int i = 0; i < response1.size(); i++) {
//                if (failed || !Arrays.equals(response1.get(i), response2.get(i))) {
//                    failed = true;
//                    break;
//                }
//            }
//            List<byte[]> response = response1;
            PrintWriter pw1 = new PrintWriter(s1.getOutputStream());
            PrintWriter pw2 = new PrintWriter(s2.getOutputStream());
            for (String line : request) {
                pw1.println(line);
                pw2.println(line);
            }
            pw1.flush();
            pw2.flush();

            InputStream is1 = s1.getInputStream();
            InputStream is2 = s2.getInputStream();
            List<byte[]> response = new LinkedList<byte[]>();
            byte[] buffer1 = new byte[BUFFER_SIZE];
            byte[] buffer2 = new byte[BUFFER_SIZE];
            int bytesRead1 = is1.read(buffer1);
            int bytesRead2 = is2.read(buffer2);
            int lastRead = 0;

            while (!failed && bytesRead1 != -1 && bytesRead2 != -1) {
                if (bytesRead1 != bytesRead2 || !Arrays.equals(buffer1, buffer2)) {
                    failed = true;
                    break;
                }
                response.add(buffer1);
                lastRead = bytesRead1;
                buffer1 = new byte[BUFFER_SIZE];
                buffer2 = new byte[BUFFER_SIZE];
                bytesRead1 = is1.read(buffer1);
                bytesRead2 = is2.read(buffer2);
            }
            is1.close();
            is2.close();
            pw1.close();
            pw2.close();
            s1.close();
            s2.close();
            System.out.println("failed: " + failed);
            if (failed) {
                HTTPCode.code500.sendResponse(outWriter);
                return;
            }
            for (int i = 0; i < response.size(); i++) {
                System.out.printf("%d of %d\n", i + 1, response.size());
                if (i < response.size() - 1) {
                    outStream.write(response.get(i), 0, BUFFER_SIZE);
                } else {
                    outStream.write(response.get(i), 0, lastRead);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ProxyClientRunnable2.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            outWriter.flush();
            outWriter.close();
            try {
                outStream.flush();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable2.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable2.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                remoteSoc.close();
            } catch (IOException ex) {
                Logger.getLogger(ProxyClientRunnable2.class.getName()).log(Level.SEVERE, null, ex);
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

}
