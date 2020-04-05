import java.io.IOException;
import java.net.Socket;

public class ClientResponder extends Thread {
    final Socket client;
    final Socket coordinator;
    final Consistency type;

    public ClientResponder(Socket client, Socket coordinator, Consistency type) {
        this.client = client;
        this.coordinator = coordinator;
        this.type = type;
    }

    @Override
    public void run(){
        try {
            System.out.println("Waiting for the client to join");
            String[] message = ServerHelper.receiveMessageFromClient(client);
            ServerHelper.processMessageFromClient(client, coordinator, type, message,Server.articleList,Server.dependencyList);
            client.close();
            System.out.println("Socket Closed");
        } catch (IOException e) {
            switch(type){
                case SEQUENTIAL:
                    String[] message = ServerHelper.receiveMessageFromClient(client);
                    try {
                        ServerHelper.processMessageFromClient(client, coordinator, type, message,Server.articleList,Server.dependencyList);
                    } catch (IOException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                case QUORUM:
                case READ_YOUR_WRITE:
                    String[] clientMessage = ServerHelper.receiveMessageFromClient(client);
                    try {
                        ServerHelper.processMessageFromClient(client, coordinator, type, clientMessage,Server.articleList,Server.dependencyList);
                    } catch (IOException | ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    ServerHelper.receiveMapsfromCoordinator(coordinator);
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't close client socket");
        }

    }
}
