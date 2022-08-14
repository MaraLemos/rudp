
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;

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
        byte [] tam = new byte[4];
        byte [] FIN = new byte[4];
        ByteArrayOutputStream bufferRecepcao = new ByteArrayOutputStream();
        Boolean fim = false;
        byte [] datagrama = new byte[1024];
        int begin = 0;
        while(true && !fim){
            
            socket.receive(new DatagramPacket(datagrama, datagrama.length));

            System.out.println("[RECEBEU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());
            System.arraycopy(datagrama, 0, seq, 0, 4);
            System.out.println("[RECEBEU] numero de sequencia " + ByteBuffer.wrap(seq).getInt());

            System.arraycopy(datagrama, 4, tam, 0, 4);
            System.out.println("[RECEBEU] tam do pacote " + ByteBuffer.wrap(tam).getInt());

            System.arraycopy(datagrama, 8, FIN, 0, 4);
            System.out.println("[RECEBEU] fin " + ByteBuffer.wrap(FIN).getInt());

            byte [] packet = new byte[ByteBuffer.wrap(tam).getInt()];
            System.arraycopy(datagrama, 12, packet, 0, packet.length);
            System.out.println("[RECEBEU] pacote");

            //Escreve pacote no buffer de recepção
            bufferRecepcao.write(packet,0,packet.length);

            begin += datagrama.length;
            byte [] ack = ByteBuffer.allocate(4).putInt(begin).array();
            socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
            System.out.println("[ENVIOU] akc " + begin);

            if(ByteBuffer.wrap(tam).getInt() == 1)
                fim = true;

            System.out.println("");
        }


        System.out.println("Tamanho do arquivo final " + bufferRecepcao.size());

        //Reconstroi arquivo
        File file = new File("recebido.pdf");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bufferRecepcao.toByteArray());
        fos.flush();
        fos.close();
    }
}
