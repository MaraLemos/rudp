
import java.io.FileInputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author 
 */
public class Cliente {
    public static String PATH = "C:\\Users\\lucas\\OneDrive\\Documentos\\Programacao\\Redes\\rudp\\teste.pdf";
    
    public static List<byte[]> getPackets(String path) throws Exception {
        List<byte[]> packets = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1024)
                packets.add(file.readNBytes(1024));
        }
        
        return packets;
    }
    
    // ByteBuffer.wrap(bytes).getInt() ==> array de byte para int
    // ByteBuffer.allocate(4).putInt(begin).array(); ==> int para array de byte
    
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(2000);
        
        List<byte[]> packets = getPackets(PATH);
        
        int index = 0, begin = 0;
        while(true && index < 1104) {
            byte [] packet = packets.get(index);
            
            System.out.println("[ENVIOU] seq = " + begin);
            byte [] seq = ByteBuffer.allocate(4).putInt(begin).array();
            socket.send(new DatagramPacket(seq, seq.length, InetAddress.getLocalHost(), 1000));
            
            System.out.println("[ENVIOU] pacote " + index);
            socket.send(new DatagramPacket(packet, packet.length, InetAddress.getLocalHost(), 1000));
            
            byte [] ack = new byte[4];
            socket.receive(new DatagramPacket(ack, ack.length));
            System.out.println("[RECEBEU] ack = " + ByteBuffer.wrap(ack).getInt());
            
            begin += packet.length;
            index++;
            
            System.out.println("");
        }
    }
}
