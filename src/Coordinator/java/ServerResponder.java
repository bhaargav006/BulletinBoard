import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerResponder extends Thread {
    Socket server;
    Consistency type;

    public ServerResponder(Socket server, Consistency type){
        this.server = server;
        this.type = type;
        Coordinator.serverSockets.add(server);
    }

    @Override
    public void run(){
        try {

            switch (type) {
                case SEQUENTIAL: {
                    CoordinatorHelper.sendConsistencyTypeToServers(server, Consistency.SEQUENTIAL.toString());
                    while (!type.equals(Consistency.ERROR)) {
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> newPair = CoordinatorHelper.processMessageReceivedFromServer(server, type, pair.getKey(), pair.getValue(), Coordinator.ID);
                        CoordinatorHelper.broadcastMessageToServers(newPair.getKey(), newPair.getValue());
                    }
                    server.close();
                    System.out.println("Socket Closed");
                    break;
                }

                case QUORUM:
                    CoordinatorHelper.sendConsistencyTypeToServers(server, Consistency.QUORUM.toString());
                    HashMap<String, Integer> readWriteNumbers = CoordinatorHelper.getReadAndWriteServers();
                    boolean valid = CoordinatorHelper.validReadWriteServerValues(readWriteNumbers.get("Read"), readWriteNumbers.get("Write"), Coordinator.serverSockets.size());
                    if(!valid){
                        System.out.println("Invalid number of read and write servers!");
                        System.out.println("Closing Sockets");
                        server.close();
                        break;
                    }

                    ArrayList<Socket> readServers = CoordinatorHelper.getReadServers(readWriteNumbers.get("Read"));
                    ArrayList<Socket> writeServers = CoordinatorHelper.getWriteServers(readWriteNumbers.get("Write"));

                    Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                    Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> quorumPair = CoordinatorHelper.getArticlesFromCoordinator(server, readServers);
                    CoordinatorHelper.sendArticlesToServers(server, quorumPair);

                    break;
                case READ_YOUR_WRITE:
                    CoordinatorHelper.sendConsistencyTypeToServers(server, Consistency.READ_YOUR_WRITE.toString());
                    Pair<String, HashMap<Integer, ArrayList<Integer>>> rywPair = CoordinatorHelper.receiveMessageFromServer(server);
                    Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> globalPair = CoordinatorHelper.getArticlesFromCoordinator(server, Coordinator.serverSockets);
                    CoordinatorHelper.sendArticlesToServers(server, globalPair);
                    break;
                case ERROR:
                    break;
                case EXIT:
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Couldn't close server sockets");;
        }

    }
}

