package Source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ServerUDP {
    private DatagramSocket socket = null;

    public ServerUDP() {
        try {
        this.socket = new DatagramSocket(54321);
        } catch (Exception e) {
        }
    }

    public ServerUDP(int port) {
        try {
        this.socket = new DatagramSocket(port);
        } catch (Exception e) {
        }
    }

   public boolean send(String s, InetSocketAddress client) {
        try{
            byte[] buf = new byte[1024];
            buf = s.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, client.getAddress(), client.getPort());
            socket.send(packet);
        }
        catch(IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
   } 

   public DatagramPacket recieve() {
        byte[] buf = new byte[1024];
        DatagramPacket p = new DatagramPacket(buf, 1024); 
        try {
            socket.receive(p);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return p;
   }
   
}
