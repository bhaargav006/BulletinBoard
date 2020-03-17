import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Coordinator {

    HashMap<String, String> serverMessageQueue = new HashMap<>();
    int ID = 0;
    public Coordinator(int port) throws IOException {
        InetAddress host = InetAddress.getLocalHost();
        ServerSocket server = new ServerSocket(port);
        Socket socket = server.accept();
        String message = CoordinatorHelper.receiveMessageFromServer(socket, serverMessageQueue, ID);
        CoordinatorHelper.broadcastMessageToServers(socket, message);
        socket.close();
        System.out.println("Socket Closed");
    }

    public static void main(String[] args) throws IOException {
        Coordinator coordinator = new Coordinator(8001);
    }
}

