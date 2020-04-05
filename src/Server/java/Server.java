import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    Socket coordinatorSocket;
    static volatile HashMap<Integer, String> articleList;
    static volatile HashMap<Integer, ArrayList<Integer>> dependencyList;
    Consistency type;
    ServerSocket server;


    public Server(int port, String arg) {

        articleList = new HashMap<>();
        dependencyList = new HashMap<>();
        type = Enum.valueOf(Consistency.class,arg);
        server = null;
        Thread syncthread = null;
        if(Consistency.QUORUM == type) {
            syncthread = new QuorumSyncThread();
            new Thread(syncthread).start();
        }
        try {
            InetAddress host = InetAddress.getLocalHost();
            coordinatorSocket = new Socket(host, 8001);
            server = new ServerSocket(port);

            while(true) {

                Socket client = null;
                try {
                    client = server.accept();

                    Thread clientResponder = new ClientResponder(client, coordinatorSocket);
                    clientResponder.start();

                } catch (IOException e) {
                    client.close();
                    ((QuorumSyncThread) syncthread).stopSync();
                    System.out.println("Error in the server sockets while accepting clients");
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Couldn't get the host of the Server");
        } catch (IOException e) {
            System.out.println("Couldn't create connection to the Coordinator ");
        }
    }
    public static void main(String[] args)  {

        Server server = new Server(8000, "QUORUM");
    }
}
