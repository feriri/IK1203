import tcpclient.TCPClient;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HTTPAsk {
    static int BUFFERSIZE = 1024;
    static boolean shutdown = false;             // True if client should shutdown connection
    static Integer timeout = null;			     // Max time to wait for data from server (null if no limit)
    static Integer limit = null;			     // Max no. of bytes to receive from server (null if no limit)
    static String hostname = null;			     // Domain name of server
    static Integer port = null;					 // Server port number
    static String string = "";

    public static void main( String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        System.out.println("Started server at " + serverSocket.getLocalPort());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFERSIZE];
        int byteRead;
        boolean on = true;

        String ok = "HTTP/1.1 200 OK\r\n\r\n";
        String not_found = "HTTP/1.1 404 Not Found\r\n";
        String bad_request = "HTTP/1.1 400 Bad Request\r\n";

        while(true){
            Socket connectionSocket = serverSocket.accept();
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
                for (int i = 0; i < parts.length - 1 ; i++) {
                switch (parts[i]) {
                    case "hostname": hostname = parts[++i]; break;
                    case "port": port = Integer.parseInt(parts[++i]); break;
                    case "shutdown": shutdown = true; break;
                    case "timeout": timeout = Integer.parseInt(parts[++i]); break;
                    case "limit": limit = Integer.parseInt(parts[++i]); break;
                    case "string": string = parts[++i]; break;
                    }
                }
                if(hostname != null && port != null && parts[0].equals("GET")){
                    TCPClient tcpClient = new tcpclient.TCPClient(shutdown, timeout, limit);
                    byte[] sb  = tcpClient.askServer(hostname, port, string.getBytes());
                    os.write(ok.getBytes());
                    os.write(new String(sb).getBytes());
                } else {
                    os.write(not_found.getBytes());
                }
            } catch (IOException ex){
                os.write(bad_request.getBytes());
            }
            os.flush();
            connectionSocket.close();
            baos.close();
        }
    }
}