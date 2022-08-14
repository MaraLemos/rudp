import java.net.*;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class Client{
      
      public static void main(String args[])throws Exception{     

            DatagramSocket socket = new DatagramSocket(2000);
            
            int congWin = 1; //Janela de congestionamento

            //Leitura do arquivo e armazenamento em um array de bytes
            FileInputStream f = new FileInputStream("teste.pdf"); //arquivo com 1.129.650 bytes
            byte bufferTransmissao[] = new byte[(int)f.getChannel().size()];

            for(int i = 0; f.available() != 0; i++){
                  bufferTransmissao[i] = (byte)f.read();
            }

            f.close();

            //Divide arquivo em pacotes de no max 1024 bytes
            int qtdPacotes =  (int)(bufferTransmissao.length/1024) + 1;
            int i = 0, cont = 1;
            while(i < bufferTransmissao.length){
                  int tamDados = 1024;
                  if(cont == qtdPacotes){
                        tamDados = bufferTransmissao.length - i;
                  }
                  byte [] dados = new byte[tamDados];
                  System.arraycopy(bufferTransmissao, i, dados, 0, tamDados);
                  i += tamDados;
                  cont++;

                  //Envia pacote para o servidor
                  socket.send(new DatagramPacket(dados, dados.length, InetAddress.getLocalHost(), 1000));
                  System.out.println("Enviando um datagrama");
            }
      }

      public static int aumentoAditivo(int congWin){
            if(congWin < 10){
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
