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
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> newPair = CoordinatorHelper.processMessageReceivedFromServer(server.getSocket(),pair.getKey(), pair.getValue(), Coordinator.ID);
                        CoordinatorHelper.broadcastMessageToServers(newPair.getKey(), newPair.getValue(), Coordinator.serverSockets);
                    }
                    server.close();
                    System.out.println("Socket Closed");
                    break;
                }

                case QUORUM: {
                    if((int)server.getOis().readObject()==0){
                        server.getOos().writeObject(Coordinator.ID);
                        server.getOos().reset();
                        server.close();
                    }
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.QUORUM.toString(), oos);
                    while (!type.equals(Consistency.ERROR)) {


                        boolean valid = CoordinatorHelper.validReadWriteServerValues(Coordinator.readWriteNumbers.get("Read"), Coordinator.readWriteNumbers.get("Write"), Coordinator.serverSockets.size());
                        if (!valid) {
                            System.out.println("Invalid number of read and write servers!");
                            System.out.println("Closing Sockets");
                            server.close();
                            break;
                        }

                        ArrayList<SocketConnection> writeServers = CoordinatorHelper.getWriteServers(Coordinator.readWriteNumbers.get("Write"));
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> newPair = CoordinatorHelper.processMessageReceivedFromServer(server.getSocket(), pair.getKey(), pair.getValue(), Coordinator.ID);
                        CoordinatorHelper.broadcastMessageToServers(newPair.getKey(), newPair.getValue(), writeServers);

                    }
                    server.close();
                    System.out.println("Socket Closed");
                    break;
                }

                case READ_YOUR_WRITE: {
                    if((int)server.getOis().readObject()==0){
                        server.getOos().writeObject(Coordinator.ID);
                        server.getOos().reset();
                        server.close();
                    }
                    CoordinatorHelper.sendConsistencyTypeToServers(server.getSocket(), Consistency.READ_YOUR_WRITE.toString(), oos);
                    while (!type.equals(Consistency.ERROR)) {
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server);
                        Pair<String, HashMap<Integer, ArrayList<Integer>>> newPair = CoordinatorHelper.processMessageReceivedFromServer(server.getSocket(),pair.getKey(), pair.getValue(), Coordinator.ID);
                        CoordinatorHelper.broadcastMessageToServers(newPair.getKey(), newPair.getValue(), Coordinator.serverSockets);
                    }
                    server.close();
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

