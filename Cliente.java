
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
    public static String PATH = "video.mp4";

    public static List<byte[]> getPackets(String path) throws Exception {
        List<byte[]> packets = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1008)
                packets.add(file.readNBytes(1008));

                System.out.println(file.getChannel().size());
        }
        
        return packets;
    }
    
    // ByteBuffer.wrap(bytes).getInt() ==> array de byte para int
    // ByteBuffer.allocate(4).putInt(begin).array(); ==> int para array de byte
    
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(2000);
        
        List<byte[]> packets = getPackets(PATH);


        int begin = 0, congWin = 1;
        for(int index = 0; index < packets.size(); index += congWin) {
            for(int cont = 0; cont < congWin && (cont + index < packets.size()); cont++) {
                //Dados
                byte [] packet = packets.get(cont + index);
                
                byte [] datagrama = new byte[16 + packet.length];

                //Cabeçalho
                byte [] seq = ByteBuffer.allocate(4).putInt(begin).array();
                byte [] tam = ByteBuffer.allocate(4).putInt(packet.length).array();
                byte [] FIN = (index + cont == (packets.size() -1)) ? ByteBuffer.allocate(4).putInt(1).array() : ByteBuffer.allocate(4).putInt(0).array();
                byte [] conWin = ByteBuffer.allocate(4).putInt(congWin).array();

                System.arraycopy(seq, 0, datagrama, 0, 4);
                System.arraycopy(tam, 0, datagrama, 4, 4);
                System.arraycopy(FIN, 0, datagrama, 8, 4);
                System.arraycopy(conWin, 0, datagrama, 12, 4);
                System.arraycopy(packet, 0, datagrama, 16, packet.length);
                

                socket.send(new DatagramPacket(datagrama, datagrama.length, InetAddress.getLocalHost(), 1000));
                
                System.out.println("cont = " + cont);
                System.out.println("[ENVIOU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());
                System.out.println("[ENVIOU] tam " + ByteBuffer.wrap(tam).getInt());
                System.out.println("[ENVIOU] fin " + ByteBuffer.wrap(FIN).getInt());
                  

                begin += datagrama.length;
            }

            System.out.println("proximo ack = " + begin);
            System.out.println("Tamanho da janela = " + congWin);

            long time, time1;
            time = System.currentTimeMillis();
            do {
                time1 = System.currentTimeMillis();
                
                byte [] ack = new byte[4];
                socket.receive(new DatagramPacket(ack, ack.length));
                System.out.println("[RECEBEU] akc " + ByteBuffer.wrap(ack).getInt());

                if(ByteBuffer.wrap(ack).getInt() == begin){
                    congWin = aumentoAditivo(congWin);
                    break;
                }
                
                ///Perda de pacote
                byte [] ack2 = new byte[4];
                socket.receive(new DatagramPacket(ack2, ack2.length));

                byte [] ack3 = new byte[4];
                socket.receive(new DatagramPacket(ack3, ack3.length));

                if(ack2 != null && ack3 != null && ByteBuffer.wrap(ack).getInt() == ByteBuffer.wrap(ack2).getInt() && ByteBuffer.wrap(ack).getInt() == ByteBuffer.wrap(ack3).getInt()){
                    congWin = diminuicaoMultiplicativa(congWin);
                    index = (ByteBuffer.wrap(ack).getInt() / 1024);
                    break;
                }

            } while ((time1 - time) < (2 * 1000)) ;

            if((time1 - time) >= (2 * 1000)){
                index -= congWin;
                congWin = diminuicaoMultiplicativa(congWin);
            }
        }
    }

    public static int aumentoAditivo(int congWin){
        if(congWin < 10){ //O cliente sabe que o limiar do servidor é 10
            congWin++;
        }
        return congWin;
    }

    public static int diminuicaoMultiplicativa(int congWin){
        if(congWin > 1){
              congWin = congWin/2;
        }
        return congWin;
    }
}

