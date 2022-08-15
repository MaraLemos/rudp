
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;

public class Servidor {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(1000);

        byte [] seq = new byte[4];
        byte [] tam = new byte[4];
        byte [] FIN = new byte[4];
        byte [] conWin = new byte[4];
        ByteArrayOutputStream bufferRecepcao = new ByteArrayOutputStream();
        Boolean fim = false;
        byte [] datagrama = new byte[1024];
        int begin = 0, congWin = 1;

        int cont = 0;
        while(true && !fim){
            if(cont == congWin) {
                System.out.println("Recebeu "+ congWin +" pacotes");

                byte [] ack = ByteBuffer.allocate(4).putInt(begin).array();
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                System.out.println("[ENVIOU] akc " + begin);

                cont = 0;
            }

            socket.receive(new DatagramPacket(datagrama, datagrama.length));

            System.arraycopy(datagrama, 0, seq, 0, 4);
            System.arraycopy(datagrama, 4, tam, 0, 4);
            System.arraycopy(datagrama, 8, FIN, 0, 4);
            System.arraycopy(datagrama, 12, conWin, 0, 4);

            byte [] packet = new byte[ByteBuffer.wrap(tam).getInt()];
            System.arraycopy(datagrama, 16, packet, 0, packet.length);
            
            congWin = ByteBuffer.wrap(conWin).getInt();
            System.out.println("[RECEBEU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());
            System.out.println("[RECEBEU] tam do pacote " + ByteBuffer.wrap(tam).getInt());
            System.out.println("[RECEBEU] fin " + ByteBuffer.wrap(FIN).getInt());
            System.out.println("[RECEBEU] pacote");
            System.out.println("cont = " + cont);

            if(ByteBuffer.wrap(seq).getInt() != begin){ //Perda de pacote
                byte [] ack = ByteBuffer.allocate(4).putInt(begin).array();

                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));

            }else{
                //Escreve pacote no buffer de recepção
                bufferRecepcao.write(packet,0,packet.length);

                begin += 16 + packet.length;

                if(ByteBuffer.wrap(FIN).getInt() == 1){
                    fim = true;
    
                    System.out.println("Chegou no final");
    
                    byte [] ack = ByteBuffer.allocate(4).putInt(begin).array();
                    socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                    System.out.println("[ENVIOU] akc " + begin);
                    break;
                }

                cont++;
            }
        }

        //Reconstroi arquivo
        //File file = new File("recebido.pdf");
        File file = new File("recebido.mp4");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bufferRecepcao.toByteArray());
        fos.flush();
        fos.close();
    }
}
