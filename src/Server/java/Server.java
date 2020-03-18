import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {


    public Server(int port) throws UnknownHostException {

        HashMap<Integer, String> articleList = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> dependencyList = new HashMap<>();

        /* Getting data from the client */
        InetAddress host = InetAddress.getLocalHost();
        try {
            ServerSocket server = new ServerSocket(port);
            Socket socket = server.accept();
            String[] message = ServerHelper.receiveMessageFromClient(socket);
            ServerHelper.processMessageFromClient(socket, message,articleList,dependencyList);
            socket.close();
            System.out.println("Socket Closed");

        } catch (IOException e) {
            System.out.println("Error in the server sockets");
        }
    }
    public static void main(String[] args)  {

        try {
            Server server = new Server(8000);
        } catch (UnknownHostException e) {
            System.out.println("Host cannot be resolved");
        }
    }
}
