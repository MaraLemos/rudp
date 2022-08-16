
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

        System.out.println("SERVIDOR PRONTO");
        System.out.println("================================================");
        ByteArrayOutputStream bufferRecepcao = new ByteArrayOutputStream();

        byte [] seq = new byte[4];
        byte [] tamDados = new byte[4];
        byte [] FIN = new byte[4];
        byte [] conWin = new byte[4];
        byte [] datagrama = new byte[1024];

        Boolean fim = false;
        int ACK = 0, congWin = 1, cont = 0, numPacotes = 0;

        while(!fim){

            socket.receive(new DatagramPacket(datagrama, datagrama.length));

            System.arraycopy(datagrama, 0, seq, 0, 4);
            System.arraycopy(datagrama, 4, tamDados, 0, 4);
            System.arraycopy(datagrama, 8, FIN, 0, 4);
            System.arraycopy(datagrama, 12, conWin, 0, 4);

            byte [] dados = new byte[ByteBuffer.wrap(tamDados).getInt()];
            System.arraycopy(datagrama, 16, dados, 0, dados.length);
            
            congWin = ByteBuffer.wrap(conWin).getInt();

            if(ByteBuffer.wrap(seq).getInt() == ACK){

                numPacotes++;

                //Escreve pacote no buffer de recepção
                bufferRecepcao.write(dados,0,dados.length);
                System.out.println("[RECEBEU] pacote " + numPacotes);
                
                ACK += (16 + dados.length);

                if(ByteBuffer.wrap(FIN).getInt() == 1){
                    fim = true;

                    byte [] ack = ByteBuffer.allocate(4).putInt(ACK).array();
                    socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                    System.out.println("[ENVIOU] ACK " + ACK);
                    System.out.println("================================================");
                    System.out.println("FIM DA CONEXAO");
                    break;
                }

                cont++;

            }else{

                System.out.println("[ERRO] Pacote fora de ordem");
                System.out.println("Num seq esperado = " + ACK + " (pacote " + ((ACK/1024)+1) + ") Num seq recebido = " + ByteBuffer.wrap(seq).getInt() + " (pacote " + ((ByteBuffer.wrap(seq).getInt()/1024)+1) + ")");
                byte [] ack = ByteBuffer.allocate(4).putInt(ACK).array();

                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                
                cont = congWin;
            }

            if(cont == congWin) {
                
                byte [] ack = ByteBuffer.allocate(4).putInt(ACK).array();
                socket.send(new DatagramPacket(ack, ack.length, InetAddress.getLocalHost(), 2000));
                System.out.println("[ENVIOU] ACK " + ACK);
                System.out.println("================================================");
                cont = 0;
            }
        }

        //Reconstroi arquivo
        File file = new File("recebido.pdf");
        //File file = new File("recebido.mp4");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bufferRecepcao.toByteArray());
        fos.flush();
        fos.close();
    }
}
