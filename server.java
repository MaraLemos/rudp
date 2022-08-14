import java.net.*;
import java.io.*;
public class Server{

      public static void main(String args[])throws IOException{

            DatagramSocket socket = new DatagramSocket(1000);

            byte bufferRecepcao[] = new byte[1129650]; //Buffer de recepção de no max 1.129.650 bytes
            int posBufferRecp = 0;

            byte b[] = new byte[1024];

            while(true){

                  DatagramPacket dp = new DatagramPacket(b, b.length);
                  socket.receive(dp);
                  System.out.println("Tamanho do pacote recebido: " + dp.getLength());
                  System.arraycopy(dp.getData(), 0, bufferRecepcao, posBufferRecp, dp.getLength());
                  posBufferRecp += dp.getLength();
            }
      }
}