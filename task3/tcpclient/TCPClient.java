package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    static int BUFFERSIZE = 1024;
    static boolean shutdown;
    static Integer timeout;
    static Integer limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        byte[] serverBuffer = new byte[BUFFERSIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Socket clientSocket = new Socket();
        SocketAddress socketAddr = new InetSocketAddress(hostname, port);
        try {
            if (timeout != null) {
                clientSocket.connect(socketAddr, timeout);
                clientSocket.setSoTimeout(timeout);
            } else clientSocket.connect(socketAddr);
            InputStream is = clientSocket.getInputStream();
            clientSocket.getOutputStream().write(toServerBytes);
            if (shutdown) clientSocket.shutdownOutput();
            int byteRead;
            while (true) {
                if (limit != null) {
                    byteRead = is.read(serverBuffer, 0, limit);
                    baos.write(serverBuffer, 0, limit);
                    if (byteRead != -1 || baos.size() != limit) break;
                } else {
                    byteRead = is.read(serverBuffer, 0, serverBuffer.length);
                    if (byteRead == -1) break;
                    baos.write(serverBuffer, 0, byteRead);
                }
            }
            clientSocket.close();
            baos.close();
        }
        catch (SocketTimeoutException ex) {
            System.out.println("Timeout exception");
        }
        catch (IOException exc) {
            System.out.println("IO exception");
        }
        return baos.toByteArray();
    }
}
