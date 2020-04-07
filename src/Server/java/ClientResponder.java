import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientResponder extends Thread {
    final SocketConnection client;
    final SocketConnection coordinator;
    final Consistency type;
    final ObjectOutputStream clientOos;
    final ObjectInputStream clientOis;


//    public ClientResponder(Socket client, Socket coordinator, Consistency type, ObjectInputStream ois, ObjectOutputStream oos) {
//        this.client = client;
//        this.coordinator = coordinator;
//        this.type = type;
//    }

    public ClientResponder(SocketConnection client, SocketConnection coordinatorSocket, Consistency type) {
        this.client = client;
        this.coordinator = coordinatorSocket;
        this.type = type;
        this.clientOis = client.getOis();
        this.clientOos = client.getOos();
    }

    @Override
    public void run(){

        try {
            while (true) {
                switch (type) {
                    case SEQUENTIAL: {
                        String[] message = ServerHelper.receiveMessageFromClient(clientOis);
                        System.out.println("Request Type : " + message[0]);
                        ServerHelper.processMessageFromClient(client, coordinator, type, message, Server.articleList, Server.dependencyList, clientOos, clientOis);
                        break;
                    }
                    case QUORUM: {
                        String[] qmessage = ServerHelper.receiveMessageFromClient(clientOis);
                        System.out.println("Request Type : " + qmessage);
                        ServerHelper.processMessageFromClient(client, coordinator, type, qmessage, Server.articleList, Server.dependencyList, clientOos, clientOis);
                        break;
                    }
                    case READ_YOUR_WRITE:
                        String[] clientMessage = ServerHelper.receiveMessageFromClient(clientOis);
                        System.out.println("Message "+clientMessage[0]);
                        ServerHelper.processMessageFromClient(client, coordinator, type, clientMessage, Server.articleList, Server.dependencyList, clientOos, clientOis);
                        //   ServerHelper.receiveMapsfromCoordinator(coordinator);
                        break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Waiting for client");
        }

    }
}
