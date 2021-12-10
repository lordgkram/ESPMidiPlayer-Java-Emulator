package javaMidi.tcpmode;

import javaMidi.cppconv.Song;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTcpServer {
    private boolean active = true;
    private final Executor serverExecutor;
    private final ExecutorService playerExecutor;
    private ServerSocket serverSocket;

    public AsyncTcpServer(){
        serverExecutor = Executors.newSingleThreadExecutor();
        playerExecutor = Executors.newSingleThreadExecutor();
    }

    public void listenOnPort(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.printf("TCP server on port %d started%n", port);
        serverExecutor.execute(
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
                        String midiData = in.readLine();
                        out.printf("playing: %s%n", midiData);
                        play(midiData);
                    }catch (IOException exception){
                        exception.printStackTrace();
                    }
                }
            }
        );
    }

    public void play(String midiData){
        playerExecutor.submit(
                ()-> Song.playSong(midiData, 3600)
        );
    }

    public void stop(){
        active = false;
    }
}
