
import java.io.FileInputStream;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cliente {

    //public static String PATH = "teste.pdf";
    public static String PATH = "teste.pdf";

    public static List<byte[]> getPackets(String path) throws Exception {
        List<byte[]> packets = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1012)
                packets.add(file.readNBytes(1012));

                System.out.println(file.getChannel().size());
        }
        
        return packets;
    }
    
    // ByteBuffer.wrap(bytes).getInt() ==> array de byte para int
    // ByteBuffer.allocate(4).putInt(begin).array(); ==> int para array de byte
    
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(2000);
        
        List<byte[]> packets = getPackets(PATH);


        int begin = 0, total = 0;
        for(int index = 0; index < packets.size(); index += 10) {
            for(int cont = 0; cont < 10 && (cont + index < packets.size()); cont++) {
                //Dados
                byte [] packet = packets.get(cont + index);
                
                byte [] datagrama = new byte[12 + packet.length];

                //Cabeçalho
                byte [] seq = ByteBuffer.allocate(4).putInt(begin).array();
                byte [] tam = ByteBuffer.allocate(4).putInt(packet.length).array();
                byte [] FIN = (index + cont == (packets.size() -1)) ? ByteBuffer.allocate(4).putInt(1).array() : ByteBuffer.allocate(4).putInt(0).array();

                System.arraycopy(seq, 0, datagrama, 0, 4);
                System.arraycopy(tam, 0, datagrama, 4, 4);
                System.arraycopy(FIN, 0, datagrama, 8, 4);
                System.arraycopy(packet, 0, datagrama, 12, packet.length);
                

                socket.send(new DatagramPacket(datagrama, datagrama.length, InetAddress.getLocalHost(), 1000));
                
                System.out.println("cont = " + cont);
                System.out.println("[ENVIOU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());
                System.out.println("[ENVIOU] tam " + ByteBuffer.wrap(tam).getInt());
                System.out.println("[ENVIOU] fin " + ByteBuffer.wrap(FIN).getInt());
                  

                begin += datagrama.length;
                total++;
            }

            System.out.println("proximo ack = " + begin);

            byte [] ack = new byte[4];
            socket.receive(new DatagramPacket(ack, ack.length));
            ByteBuffer.wrap(ack).getInt();

            System.out.println("[RECEBEU] akc " + ByteBuffer.wrap(ack).getInt());

        }
    }
}
