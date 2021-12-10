package javaMidi.tcpmode;

import javaMidi.JavaMain;
import javaMidi.cppconv.Interface;
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
    private static final int SOCKET_TIMEOUT_IN_MS = 10*1000;

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
                        clientSocket.setSoTimeout(SOCKET_TIMEOUT_IN_MS);
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
        String payload = midiData.startsWith("{") ? midiData : "{\"midi\":\"" + midiData + "\", \"laenge\":3600}";
        playerExecutor.submit(
                ()-> Interface.mqttCallback(JavaMain.TOPIC_MIDI, payload)
        );
    }

    public void stop(){
        active = false;
    }
}
