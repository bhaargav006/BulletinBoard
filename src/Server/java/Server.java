import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {

    //Send the client the data during the tcp call.
    //Why can't we do a RMI
    public Server(int port) throws UnknownHostException {
        InetAddress host = InetAddress.getLocalHost();
        try {
            ServerSocket server = new ServerSocket(port);
            Socket socket = server.accept();
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println(in.readUTF());
            in.close();
            socket.close();
            System.out.println("Socket Closed");

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }
    }
    public static void main(String[] args) throws UnknownHostException {

        Server server = new Server(8000);
    }
}
