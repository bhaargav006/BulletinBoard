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
    Scanner in = new Scanner(System.in);
    String consistency = "";
    public Coordinator(int port) throws IOException, ClassNotFoundException {
        System.out.println("Hello Coordinator, Choose a type of consistency:");
        System.out.println("1. [Seq] Sequential Consistency \n2. [Quo] Quorum Consistency \n3. [RYW] Read your write Consistency");
        consistency = in.nextLine();
        switch (consistency){
            case "Seq":
                InetAddress host = InetAddress.getLocalHost();
                ServerSocket server = new ServerSocket(port);
                Socket socket = server.accept();
                CoordinatorHelper.sendConsistencyTypeToServers(socket, consistency);
                Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(socket, serverMessageQueue, ID);
                CoordinatorHelper.broadcastMessageToServers(socket, pair.getKey(), pair.getValue());
                socket.close();
                System.out.println("Socket Closed");
                break;


            case "Quo":
                InetAddress quorumHost = InetAddress.getLocalHost();
                ServerSocket quorumServer = new ServerSocket(port);
                Socket quorumSocket = quorumServer.accept();
                CoordinatorHelper.sendConsistencyTypeToServers(quorumSocket, consistency);
                HashMap<String, Integer> readWriteServers = CoordinatorHelper.getReadAndWriteServers();
                ArrayList<String> listOfServers = CoordinatorHelper.getServerIPAndPort();
                ArrayList<String> readServers = new ArrayList<>();
                ArrayList<String> writeServers = new ArrayList<>();
                Boolean valid = CoordinatorHelper.validReadWriteServerValues(readWriteServers.get("Read"), readWriteServers.get("Write"), listOfServers.size());
                if(valid){
                    for(int i = 0; i < readWriteServers.get("Read"); i++){
                        readServers.add(listOfServers.get(i));
                    }
                    for(int i = listOfServers.size() - 1; i >= (listOfServers.size() - readWriteServers.get("Write")); i--){
                        writeServers.add(listOfServers.get(i));
                    }
                }

                else{
                    System.out.println("Invalid number of read and write servers!");
//                    quorumSocket.close();
                }

                break;
            case "RYW":
            case "Exit":
                System.out.println("Bye Bye");
                break;
            default:
                System.out.println("Invalid Input");

        }

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Coordinator coordinator = new Coordinator(8001);
    }
}

