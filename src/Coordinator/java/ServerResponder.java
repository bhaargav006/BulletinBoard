import javafx.util.Pair;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerResponder extends Thread {
    Socket server;
    Consistency type;

    public ServerResponder(Socket server, Consistency type){
        this.server = server;
        this.type = type;
        Coordinator.serverSockets.add(this.server);
    }

    @Override
    public void run(){
        try {
            switch (type) {
                case SEQUENTIAL:
                    CoordinatorHelper.sendConsistencyTypeToServers(server, Consistency.SEQUENTIAL.toString());
                    Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server, Coordinator.serverMessageQueue, Coordinator.ID);
                    CoordinatorHelper.broadcastMessageToServers(server, pair.getKey(), pair.getValue());
                    server.close();
                    System.out.println("Socket Closed");
                    break;

                case QUORUM:

                    break;
                case READ_YOUR_WRITE:
                    break;
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

//                InetAddress quorumHost = InetAddress.getLocalHost();
//                ServerSocket quorumServer = new ServerSocket(port);
//                Socket quorumSocket = quorumServer.accept();
//                CoordinatorHelper.sendConsistencyTypeToServers(quorumSocket, consistency);
//                HashMap<String, Integer> readWriteServers = CoordinatorHelper.getReadAndWriteServers();
//                ArrayList<String> listOfServers = CoordinatorHelper.getServerIPAndPort();
//                ArrayList<String> readServers = new ArrayList<>();
//                ArrayList<String> writeServers = new ArrayList<>();
//                Boolean valid = CoordinatorHelper.validReadWriteServerValues(readWriteServers.get("Read"), readWriteServers.get("Write"), listOfServers.size());
//                if(valid){
//                    for(int i = 0; i < readWriteServers.get("Read"); i++){
//                        readServers.add(listOfServers.get(i));
//                    }
//                    for(int i = listOfServers.size() - 1; i >= (listOfServers.size() - readWriteServers.get("Write")); i--){
//                        writeServers.add(listOfServers.get(i));
//                    }
//                }
//
//                else{
//                    System.out.println("Invalid number of read and write servers!");
////                    quorumSocket.close();
//                }
