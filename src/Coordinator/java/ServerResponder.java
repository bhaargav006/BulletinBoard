import javafx.util.Pair;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerResponder extends Thread {
    Socket server;

    public ServerResponder(Socket server){
        this.server = server;
    }

    @Override
    public void run(){
        try {
            Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = CoordinatorHelper.receiveMessageFromServer(server, Coordinator.serverMessageQueue, Coordinator.ID);
            CoordinatorHelper.broadcastMessageToServers(server, pair.getKey(), pair.getValue());
            server.close();
            System.out.println("Socket Closed");
        } catch (IOException e) {
            System.out.println("Couldn't close server sockets");;
        }

    }
}
