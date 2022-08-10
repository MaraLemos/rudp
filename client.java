import java.net.*;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class client{

      public static void main(String args[])throws Exception{     

            DatagramSocket socket = new DatagramSocket(2000);

            //Leitura do arquivo e armazenamento em um array de bytes
            FileInputStream f = new FileInputStream("teste.pdf"); //arquivo com 1.129.650 bytes
            byte fileBytes[] = new byte[(int)f.getChannel().size()];

            for(int i = 0; f.available() != 0; i++){
                  fileBytes[i] = (byte)f.read();
            }

            f.close();

            //Divide arquivo em pacotes de no max 1024 bytes
            int qtdPacotes =  (int)(fileBytes.length/1024) + 1;
            int i = 0, cont = 1;
            while(i < fileBytes.length){
                  int tamDados = 1024;
                  if(cont == qtdPacotes){
                        tamDados = fileBytes.length - i;
                  }
                  byte [] dados = new byte[tamDados];
                  System.arraycopy(fileBytes, i, dados, 0, tamDados);
                  i += tamDados;
                  cont++;

                  //Envia pacote para o servidor
                  socket.send(new DatagramPacket(dados, dados.length, InetAddress.getLocalHost(), 1000));
                  System.out.println("Enviando um datagrama");
            }
      }

}
