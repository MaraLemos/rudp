
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Cliente {

    //public static String PATH = "teste.pdf";
    public static String PATH = "C:\\Users\\maral\\OneDrive\\Documents\\Certificado_Nacional_de_Covid-19.pdf";

    public static List<Integer> perdas = new ArrayList<>();
    public static List<byte[]> packets = new ArrayList<>();
    public static int timeout = 10;

    public static void getPackets(String path) throws Exception{
        try (FileInputStream file = new FileInputStream(path)) {
            for(int i = 0; i < (int) (file.getChannel().size()); i += 1008)
                packets.add(file.readNBytes(1008));
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        BufferedWriter saida = new BufferedWriter(new FileWriter("outputClient.txt"));

        DatagramSocket socket = new DatagramSocket(2000);

        getPackets(PATH);
        menu();

        System.out.println("Preparando para enviar arquivo = " + PATH);
        System.out.println("Quantidade de pacotes = " + packets.size());
        System.out.println("================================================");

        int numSeq = 0, congWin = 1, prox = 0;

        long tempoInicial = System.currentTimeMillis();

        int index = 0;
        while(index < packets.size()) {

            saida.write("Tamanho da janela atual = " + congWin);
            saida.newLine();
            for(int cont = 0; cont < congWin && prox < packets.size(); cont++ , index++) {

                //Dados
                byte [] dados = packets.get(prox);
                prox++;
                
                //Pulando o envio dos pacotes
                if(!perdas.isEmpty() && perdas.contains(prox)){
                    perdas.remove(perdas.indexOf(prox));
                    prox++;
                    numSeq += (16 + dados.length);
                }

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
                
                saida.write("[ENVIOU] pacote = " + prox);
                saida.newLine();
                saida.write("Valor de ACK esperado = " + (numSeq + datagrama.length));
                saida.newLine();

                numSeq += datagrama.length;
           
                socket.setSoTimeout(timeout);
                try{
                    byte [] ack = new byte[4];
                    socket.receive(new DatagramPacket(ack, ack.length));
                    saida.write("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt());
                    saida.newLine();

                    if(ByteBuffer.wrap(ack).getInt() == numSeq){

                        if(prox == packets.size()){
                            System.out.println("================================================");
                            System.out.println("FIM DA CONEXAO");
                            System.out.println("[TEMPO] " + ((System.currentTimeMillis() - tempoInicial)/1000) + "s para envio do arquivo.");
                            System.exit(0);
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
                        saida.write("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt()); saida.newLine();
                        saida.write("[RECEBEU] AKC = " + ByteBuffer.wrap(ack).getInt()); saida.newLine();
                        saida.write("[ERRO] Ocorreu perda de pacote"); saida.newLine(); saida.newLine();
                        saida.write("O PACOTE = " + ((ByteBuffer.wrap(ack).getInt()/1024)+1) + " SERA REENVIADO"); saida.newLine();
                        saida.write("================================================"); saida.newLine();
                        congWin = diminuicaoMultiplicativa(congWin);
                        prox = (ByteBuffer.wrap(ack).getInt() / 1024);
                        numSeq = ByteBuffer.wrap(ack).getInt();
                        break;
                    }
                    
                }catch(SocketTimeoutException e){
                    //Timeout
                    if(cont == congWin){
                        prox -= congWin;
                        congWin = diminuicaoMultiplicativa(congWin);
                    }
                    break;
                }
            }
        }

        saida.close();
        socket.close();
    }

    public static int aumentoAditivo(int congWin){
        if(congWin < 50){ //O cliente sabe que o limiar do servidor é 50
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

    public static void menu(){
        System.out.println("1 - Execucao sem perdas");
        System.out.println("2 - Execucao com perdas");

        Scanner teclado = new Scanner(System.in);
        String op = teclado.next();
        
        switch(op){
            case "1":   break;

            case "2":   preencheperdas(teclado);
                        break;

            default:    System.out.println("Erro! Opcao invalida.");
                        menu();
        }
    }

    public static void preencheperdas(Scanner teclado){
        try {

            timeout = 200;

            System.out.println("Informe a quantidade de perdas.");
            int qtdperdas = Integer.parseInt(teclado.next());

            if(qtdperdas > 0 && qtdperdas < packets.size()){
                for(int i=0; i < qtdperdas; i++){
                    Random rand = new Random();
                    perdas.add(Math.abs(rand.nextInt(packets.size()-1))+1);
                }
            }else{
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("Valor invalido, digite novamente.");
            preencheperdas(teclado);
        }
    }
}

