import java.net.*;
import java.io.*;

public class ConcHTTPAsk {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.println("Started server at " + serverSocket.getLocalPort());
        try {
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                MyRunnable run = new MyRunnable(connectionSocket);
                new Thread(run).start();
            }
        } catch (IOException e) {
            System.out.println("Error");
        }
    }
}