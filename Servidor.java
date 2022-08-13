
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.nio.ByteBuffer;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 
 */
public class Servidor {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1000);

        byte [] seq = new byte[4];
        
        int index = 0, begin = 0;
        while(true && index < 1104){
            socket.receive(new DatagramPacket(seq, seq.length));
            System.out.println("[RECEBEU] seq = " + ByteBuffer.wrap(seq).getInt());
              
            byte [] packet = new byte[1024];
            socket.receive(new DatagramPacket(packet, packet.length));
            System.out.println("[RECEBEU] pacote " + index + "; tamanho = " + packet.length);
            index++;
            
            begin += packet.length;
            byte [] ack = ByteBuffer.allocate(4).putInt(begin).array();
            socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
            System.out.println("[ENVIOU] akc " + begin);
            
            System.out.println("");
        }
    }
}
