import javafx.util.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Coordinator {

    HashMap<String, String> serverMessageQueue = new HashMap<>();
    int ID = 0;
    ArrayList<String> serverIPAndPort = new ArrayList<>();
    public Coordinator(int port) throws IOException, ClassNotFoundException {

        InetAddress host = InetAddress.getLocalHost();
        ServerSocket server = new ServerSocket(port);
        Socket socket = server.accept();
        Pair<String, HashMap<Integer, Integer>> pair = CoordinatorHelper.receiveMessageFromServer(socket, serverMessageQueue, ID);
        CoordinatorHelper.broadcastMessageToServers(socket, pair.getKey(), pair.getValue());
        socket.close();
        System.out.println("Socket Closed");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Coordinator coordinator = new Coordinator(8001);
    }
}

