
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
    public static String PATH = "teste.pdf";
    
    public static List<byte[]> getPackets(String path) throws Exception {
        List<byte[]> packets = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1012)
                packets.add(file.readNBytes(1012));
        }
        
        return packets;
    }
    
    // ByteBuffer.wrap(bytes).getInt() ==> array de byte para int
    // ByteBuffer.allocate(4).putInt(begin).array(); ==> int para array de byte
    
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(2000);
        
        List<byte[]> packets = getPackets(PATH);
        int index = 0, begin = 0, ackk = 0;

        while(index < packets.size()) {

            if(ackk == begin){

                //Dados
                byte [] packet = packets.get(index);
                
                byte [] datagrama = new byte[12 + packet.length];

                //Cabeçalho
                byte [] seq = ByteBuffer.allocate(4).putInt(begin).array();
                byte [] tam = ByteBuffer.allocate(4).putInt(packet.length).array();
                byte [] FIN = (index == (packets.size() -1)) ? ByteBuffer.allocate(4).putInt(1).array() : ByteBuffer.allocate(4).putInt(0).array();

                System.arraycopy(seq, 0, datagrama, 0, 4);
                System.out.println("[ENVIOU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());
                System.arraycopy(tam, 0, datagrama, 4, 4);
                System.out.println("[ENVIOU] tam " + ByteBuffer.wrap(tam).getInt());
                System.arraycopy(FIN, 0, datagrama, 8, 1);
                System.out.println("[ENVIOU] fin " + ByteBuffer.wrap(FIN).getInt());
                System.arraycopy(packet, 0, datagrama, 12, packet.length);
                socket.send(new DatagramPacket(tam, tam.length, InetAddress.getLocalHost(), 1000));

                index++;
                begin += datagrama.length;

                byte [] ack = new byte[4];
                socket.receive(new DatagramPacket(ack, ack.length));
                ackk = ByteBuffer.wrap(ack).getInt(); 
            }
        }
    }
}
