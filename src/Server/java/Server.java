import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    SocketConnection coordinatorSocket;
    static volatile HashMap<Integer, String> articleList;
    static volatile HashMap<Integer, ArrayList<Integer>> dependencyList;
    ServerSocket server;
    Consistency type;

    public Server(int port) {

        articleList = new HashMap<Integer, String>();
        dependencyList = new HashMap<Integer, ArrayList<Integer>>();
        server = null;

        try {
            coordinatorSocket = new SocketConnection(8001);
            server = new ServerSocket(port);
            coordinatorSocket.getOos().writeObject(1);
            type = ServerHelper.getConsistencyType(coordinatorSocket);
            System.out.println(type.toString());
//            if(type.equals(Consistency.QUORUM) || type.equals(Consistency.READ_YOUR_WRITE)) {
//                coordinatorSocket.getOos().write(1);
//            }
            Thread syncthread = null;
//            if(Consistency.QUORUM == type) {
//                syncthread = new QuorumSyncThread();
//                new Thread(syncthread).start();
//            }
            while(true) {

                Socket client = null;
                try {
                    client = server.accept();
                    SocketConnection sc = new SocketConnection(client);
                    Thread clientResponder = new ClientResponder(sc,coordinatorSocket, type);
                    clientResponder.start();
                } catch (IOException e) {
                    client.close();
//                    ((QuorumSyncThread) syncthread).stopSync();
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

        Server server = new Server(Integer.parseInt(args[0]));
    }
}
