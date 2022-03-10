package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    static int BUFFERSIZE = 1024;

    public TCPClient() {
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        byte[] serverBuffer = new byte[BUFFERSIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Socket clientSocket = new Socket(hostname, port);
        InputStream is = clientSocket.getInputStream();
        OutputStream os = clientSocket.getOutputStream();
        try {
            os.write(toServerBytes);
            while (true) {
                int byteRead = is.read(serverBuffer, 0, serverBuffer.length);
                if (byteRead == -1) break;
                baos.write(serverBuffer, 0, byteRead);
            }
            clientSocket.close();
            baos.close();
        } catch (IOException ex) {
            System.out.println("Error");
        }
        return baos.toByteArray();
    }
}