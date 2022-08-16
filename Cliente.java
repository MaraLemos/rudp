
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

    public static String PATH = "teste.pdf";
    //public static String PATH = "video.mp4";

    public static List<byte[]> getPackets(String path) throws Exception {
        List<byte[]> packets = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1008)
                packets.add(file.readNBytes(1008));
        }
        
        return packets;
    }
    
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(2000);
        
        System.out.println("Preparando para enviar arquivo = " + PATH);
        List<byte[]> packets = getPackets(PATH);
        System.out.println("Quantidade de pacotes = " + packets.size());
        System.out.println("================================================");

        int numSeq = 0, congWin = 1, prox = 0;
        for(int index = 0; index < packets.size(); ) {
            System.out.println("Tamanho da janela atual = " + congWin);
            for(int cont = 0; cont < congWin && prox < packets.size(); cont++ , index++) {

                //Dados
                byte [] dados = packets.get(prox);
                prox++;

                if(prox == 3)
                    prox++;
                    
                int tamDatagrama = 16 + dados.length;
                byte [] datagrama = new byte[tamDatagrama];

                //Cabeçalho
                byte [] seq = ByteBuffer.allocate(4).putInt(numSeq).array();
                byte [] tamDados = ByteBuffer.allocate(4).putInt(dados.length).array();
                byte [] FIN = (prox == packets.size()) ? ByteBuffer.allocate(4).putInt(1).array() : ByteBuffer.allocate(4).putInt(0).array();
                byte [] conWin = ByteBuffer.allocate(4).putInt(congWin).array();

                System.arraycopy(seq, 0, datagrama, 0, 4);
                System.arraycopy(tamDados, 0, datagrama, 4, 4);
                System.arraycopy(FIN, 0, datagrama, 8, 4);
                System.arraycopy(conWin, 0, datagrama, 12, 4);
                System.arraycopy(dados, 0, datagrama, 16, dados.length);
                

                socket.send(new DatagramPacket(datagrama, datagrama.length, InetAddress.getLocalHost(), 1000));
                
                System.out.println("[ENVIOU] pacote = " + prox);
                System.out.println("Valor de ACK esperado = " + (numSeq + datagrama.length));

                numSeq += datagrama.length;
            }

            long time, time1;
            time = System.currentTimeMillis();
            do {
                time1 = System.currentTimeMillis();
                
                byte [] ack = new byte[4];
                socket.receive(new DatagramPacket(ack, ack.length));
                System.out.println("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt());

                if(ByteBuffer.wrap(ack).getInt() == numSeq){

                    System.out.println("================================================");
                    if(prox == packets.size()){
                        System.out.println("FIM DA CONEXAO");
                    }
                    congWin = aumentoAditivo(congWin);
                    break;
                }
                
                ///Perda de pacote
                byte [] ack2 = new byte[4];
                socket.receive(new DatagramPacket(ack2, ack2.length));

                byte [] ack3 = new byte[4];
                socket.receive(new DatagramPacket(ack3, ack3.length));

                //Recebimento de 3 acks iguais identifica perda de pacote
                if(ack2 != null && ack3 != null && ByteBuffer.wrap(ack).getInt() == ByteBuffer.wrap(ack2).getInt() && ByteBuffer.wrap(ack).getInt() == ByteBuffer.wrap(ack3).getInt()){
                    System.out.println("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt());
                    System.out.println("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt());
                    System.out.println("[ERRO] Ocorreu perda de pacote");
                    System.out.println("O PACOTE = " + (ByteBuffer.wrap(ack).getInt()/1024) + " SERA REENVIADO");
                    congWin = diminuicaoMultiplicativa(congWin);
                    prox = (ByteBuffer.wrap(ack).getInt() / 1024);
                    System.out.println("prox " + prox);
                    numSeq = ByteBuffer.wrap(ack).getInt();
                    System.out.println("prox " + numSeq);
                    break;
                }

            } while ((time1 - time) < (2 * 1000)) ;

            if((time1 - time) >= (2 * 1000)){
                prox -= congWin;
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

