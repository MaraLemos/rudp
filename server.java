import java.net.*;
import java.io.*;
public class server
{
      public static void main(String args[])throws IOException{

            DatagramSocket socket = new DatagramSocket(1000);

            byte b[] = new byte[1024];

            while(true){
                  DatagramPacket dp = new DatagramPacket(b, b.length);
                  socket.receive(dp);
                  System.out.println("Recebendo um datagrama");                          
            }
      }
}