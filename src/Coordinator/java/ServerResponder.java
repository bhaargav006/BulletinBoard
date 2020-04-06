import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerResponder extends Thread {
    SocketConnection server;
    Consistency type;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;

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
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server, Coordinator.ID);
                        System.out.println("in cooridnator " + pair.getKey()+ " " + pair.getValue());
                        CoordinatorHelper.broadcastMessageToServers(pair.getKey(), pair.getValue());
                    }
                    server.close();
                    System.out.println("Socket Closed");
                    break;
                }

                case QUORUM:
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.QUORUM.toString(), oos);
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

                    Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server, Coordinator.ID);


                    break;
//                case READ_YOUR_WRITE:
//                    CoordinatorHelper.sendConsistencyTypeToServers(server, Consistency.SEQUENTIAL.toString(), oos);
//                    Pair<String, HashMap<Integer, ArrayList<Integer>>> rywPair = CoordinatorHelper.receiveMessageFromServer(server, Coordinator.ID);
//                    Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> globalPair = CoordinatorHelper.getArticlesFromCoordinator(server);
//                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
//                    objectOutputStream.writeObject(globalPair);
//                    break;
                case ERROR:
                    break;
                case EXIT:
                    break;
            }
        } catch (IOException e) {
            System.out.println("Couldn't close server sockets");;
        }

    }
}

