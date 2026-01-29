package Source;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class WelcomeTCP implements Runnable {
    private ServerSocket server;
    private volatile boolean stop = false;

    public WelcomeTCP() {
        try {
            server = new ServerSocket(0); // bind to any port
            System.out.println( "TCP port:" + getPort());
        } catch (IOException ex) {
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Socket client = server.accept();
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                final String line = in.readLine().strip();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            File f = new File("./" + line);
                            FileInputStream fstream = new FileInputStream(f);

                            out.writeLong(f.length());

                            byte[] b = new byte[1024];
                            int size = 0;
                            while ((size = fstream.read(b)) != -1) {
                                out.write(b, 0, size);
                                out.flush();
                            }

                            fstream.close();
                            out.close();
                            in.close();
                        } catch (Exception e) {
                            
                        }
                    }
                }.start();

            } catch (Exception e) {

            }
        }
    }

    public void stop() {
        try {
            stop = true;
            server.close();
        } catch (IOException e) {
        }
    }

    public InetAddress getAddress() {
        return server.getInetAddress();
    }

    public int getPort() {
        return server.getLocalPort();
    }
    public void download(String filename, InetSocketAddress address) {

        try {
            Socket socket = new Socket("localhost", address.getPort());
            socket.setSoTimeout(3000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.write("./" + filename + "\n");
            out.flush();
            
            double fsize = in.readLong();
            System.out.println("Downloading " + filename);
            File f = new File("./" + filename);
            f.createNewFile();
            FileOutputStream fstream = new FileOutputStream(f);

            byte[] b = new byte[1024];
            int size = 0;
            double dsize = 1;
            while ((size = in.read(b)) != -1) {
                fstream.write(b, 0, size);
                dsize += size;
                System.out.print( String.format("Status: %.2f %% \r", (dsize/fsize)*100));
            }
            System.out.println("Status: 100%    ");

            in.close();
            out.close();
            fstream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
