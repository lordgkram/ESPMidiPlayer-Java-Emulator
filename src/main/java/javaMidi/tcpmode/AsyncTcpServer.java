package javaMidi.tcpmode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncTcpServer {
    private boolean active = true;
    private final Executor executor;
    private ServerSocket serverSocket;

    public AsyncTcpServer(){
        executor = Executors.newSingleThreadExecutor();
    }

    public void listenOnPort(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.printf("TCP server on port %d started%n", port);
        executor.execute(
            ()->{
                while(active){
                    try (
                            Socket clientSocket = serverSocket.accept();
                            OutputStream outputStream = clientSocket.getOutputStream();
                            PrintWriter out = new PrintWriter(outputStream, true);
                            InputStream inputStream = clientSocket.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader in = new BufferedReader(inputStreamReader)
                    ){
                        String input = in.readLine();
                        out.printf("echo: [%s]%n", input);
                    }catch (IOException exception){
                        exception.printStackTrace();
                    }
                }
            }
        );
    }

    public void stop(){
        active = false;
    }
}
