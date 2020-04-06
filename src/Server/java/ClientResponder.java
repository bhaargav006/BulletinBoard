import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientResponder extends Thread {
    final Socket client;
    final SocketConnection coordinator;
    final Consistency type;
    final ObjectOutputStream clientOos;
    final ObjectInputStream clientOis;


//    public ClientResponder(Socket client, Socket coordinator, Consistency type, ObjectInputStream ois, ObjectOutputStream oos) {
//        this.client = client;
//        this.coordinator = coordinator;
//        this.type = type;
//    }

    public ClientResponder(Socket client, SocketConnection coordinatorSocket, Consistency type, ObjectInputStream clOis, ObjectOutputStream clOos) {
        this.client = client;
        this.coordinator = coordinatorSocket;
        this.type = type;
        this.clientOis = clOis;
        this.clientOos = clOos;
    }

    @Override
    public void run(){

        try {
            while (true) {
                switch (type) {
                    case SEQUENTIAL:
                        String[] message = ServerHelper.receiveMessageFromClient(client, clientOis);
                        System.out.println("Request Type : " + message[0]);
                        ServerHelper.processMessageFromClient(client, coordinator, type, message, Server.articleList, Server.dependencyList, clientOos, clientOis);
                        break;
                    case QUORUM:
                        break;
                    case READ_YOUR_WRITE:
                        String[] clientMessage = ServerHelper.receiveMessageFromClient(client, clientOis);
                        try {
                            ServerHelper.processMessageFromClient(client, coordinator, type, clientMessage, Server.articleList, Server.dependencyList, clientOos, clientOis);
                            ServerHelper.receiveMapsfromCoordinator(coordinator);
                        } catch (IOException | ClassNotFoundException e1) {
                            e1.printStackTrace();
                        }
                        break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("error in Cleint Responder" + e);
        }

    }
}
