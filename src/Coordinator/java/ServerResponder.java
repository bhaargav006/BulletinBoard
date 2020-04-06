import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerResponder extends Thread {
    SocketConnection server;
    Consistency type;
    ObjectOutputStream oos;
    ObjectInputStream ois;

    public ServerResponder(SocketConnection server, Consistency type){
        this.server = server;
        this.type = type;
        Coordinator.serverSockets.add(server);
        this.oos = server.getOos();
        this.ois = server.getOis();
    }

    @Override
    public void run(){
        try {

            switch (type) {
                case SEQUENTIAL: {
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.SEQUENTIAL.toString(), oos);
                    while (!type.equals(Consistency.ERROR)) {

                        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> newPair = CoordinatorHelper.processMessageReceivedFromServer(server.getSocket(), type, pair.getKey(), pair.getValue(), Coordinator.ID);
                        CoordinatorHelper.broadcastMessageToServers(newPair.getKey(), newPair.getValue());
                    }
                    server.close();
                    System.out.println("Socket Closed");
                    break;
                }

                case QUORUM: {
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.QUORUM.toString(), oos);
                    HashMap<String, Integer> readWriteNumbers = CoordinatorHelper.getReadAndWriteServers();
                    boolean valid = CoordinatorHelper.validReadWriteServerValues(readWriteNumbers.get("Read"), readWriteNumbers.get("Write"), Coordinator.serverSockets.size());
                    if (!valid) {
                        System.out.println("Invalid number of read and write servers!");
                        System.out.println("Closing Sockets");
                        server.close();
                        break;
                    }

                    ArrayList<SocketConnection> readServers = CoordinatorHelper.getReadServers(readWriteNumbers.get("Read"));
                    ArrayList<SocketConnection> writeServers = CoordinatorHelper.getWriteServers(readWriteNumbers.get("Write"));


                    Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                    if(pair.getValue()==null){
                        Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> quorumPair = CoordinatorHelper.getArticlesFromCoordinator(server.getSocket(), readServers);
                        CoordinatorHelper.sendArticlesToServers(server, quorumPair);
                    }
                    else {
                        for (SocketConnection sc : writeServers){
                            //CoordinatorHelper.sendArticlesToServers(sc, quorumPair);
                        }
                    }

                    break;
                }

                case READ_YOUR_WRITE: {
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.READ_YOUR_WRITE.toString(), oos);
                    Pair<String, HashMap<Integer, ArrayList<Integer>>> rywPair = CoordinatorHelper.receiveMessageFromServer(server);
                    Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> globalPair = CoordinatorHelper.getArticlesFromCoordinator(server.getSocket(), Coordinator.serverSockets);
                    CoordinatorHelper.sendArticlesToServers(server, globalPair);
                    break;
                }

                case ERROR:
                    break;
                case EXIT:
                    break;
            }
        } catch (IOException e) {
            System.out.println("Couldn't close server sockets");;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}

