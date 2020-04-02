import java.io.IOException;
import java.net.Socket;

public class ClientResponder extends Thread {
    final Socket client;
    final Socket coordinator;

    public ClientResponder(Socket client, Socket coordinator) {
        this.client = client;
        this.coordinator = coordinator;
    }

    @Override
    public void run(){
        try {
            String[] message = ServerHelper.receiveMessageFromClient(client);
            ServerHelper.processMessageFromClient(client, coordinator, message,Server.articleList,Server.dependencyList);
            client.close();
            System.out.println("Socket Closed");
        } catch (IOException e) {
            System.out.println("Couldn't close client socket");
        }

    }
}
