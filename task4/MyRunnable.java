import tcpclient.TCPClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MyRunnable implements Runnable{
    Socket connectionSocket;

    public MyRunnable(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public void run() {
        int BUFFERSIZE = 1024;
        boolean shutdown = false;           // True if client should shutdown connection
        Integer timeout = null;			    // Max time to wait for data from server (null if no limit)
        Integer limit = null;			    // Max no. of bytes to receive from server (null if no limit)
        String hostname = null;			    // Domain name of server
        Integer port = null;			    // Server port number
        String string = "";

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFERSIZE];
            int byteRead;
            boolean on = true;

            String ok = "HTTP/1.1 200 OK\r\n\r\n";
            String not_found = "HTTP/1.1 404 Not Found\r\n";
            String bad_request = "HTTP/1.1 400 Bad Request\r\n";
            InputStream is = connectionSocket.getInputStream();
            OutputStream os = connectionSocket.getOutputStream();

            while (on) {
                byteRead = is.read(buffer);
                baos.write(buffer, 0, byteRead);
                if(byteRead == -1 || new String(buffer).contains("HTTP/1.1")) on = false;
            }
            String s = new String(buffer, StandardCharsets.UTF_8);

            String[] parts = s.split("[?&= ]");
            try {
                for (int i = 0; i < parts.length ; i++) {
                    switch (parts[i]) {
                        case "hostname": hostname = parts[++i]; break;
                        case "port": port = Integer.parseInt(parts[++i]); break;
                        case "shutdown": shutdown = true; break;
                        case "timeout": timeout = Integer.parseInt(parts[++i]); break;
                        case "limit": limit = Integer.parseInt(parts[++i]); break;
                        case "string": string = parts[++i]; break;
                    }
                }
                if(hostname != null && port != null && parts[0].equals("GET") && parts[1].equals("/ask")){
                    TCPClient tcpClient = new TCPClient(shutdown, timeout, limit);
                    byte[] sb  = tcpClient.askServer(hostname, port, string.getBytes());
                    os.write(ok.getBytes());
                    os.write(new String(sb).getBytes());
                }
                else {
                    os.write(not_found.getBytes());
                }
            } catch (IOException ex){
                os.write(bad_request.getBytes());
            }
            os.flush();
            connectionSocket.close();
            baos.close();

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}