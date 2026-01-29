package Source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


class ClientUDP {

    private DatagramSocket socket = null;
    ScheduledExecutorService heartBeat = null;
    private InetSocketAddress server = new InetSocketAddress("localhost", 54321); 
    private final int HEART_DELAY = 2;
    

    public ClientUDP() {
        try {
        this.socket = new DatagramSocket();
        } catch (Exception e) {
        }
    }

    public ClientUDP(int port) {
        try {
        this.socket = new DatagramSocket();
        server = new InetSocketAddress(port);
        } catch (Exception e) {
        }
    }

   public boolean send(String s) {
        try{
            byte[] buf = new byte[1024];
            buf = s.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(buf, buf.length, server.getAddress(), server.getPort());
            socket.send(p);
        }
        catch(IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
   } 

   public String recieve() {
        byte[] buf = new byte[1024];
        DatagramPacket p = new DatagramPacket(buf, 1024);
        try {
            socket.receive(p);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return new String(p.getData(), p.getOffset(),p.getLength(),StandardCharsets.UTF_8);
   }
   
   public void startHeart() {
        Runnable _heartBeat = new Thread() {
            @Override
            public void run() {
                if (!socket.isClosed()) {
                    send("ping");
            }
            }
        };
        heartBeat = new ScheduledThreadPoolExecutor(1);

        heartBeat.scheduleAtFixedRate(_heartBeat, 0, HEART_DELAY, TimeUnit.SECONDS);
   }

   public void stop() {
    heartBeat.shutdown();
    socket.close();
   }

}
