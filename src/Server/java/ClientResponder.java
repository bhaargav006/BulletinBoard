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
//        ObjectInputStream in = null;
//        try {
//            in = new ObjectInputStream(client.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {

            while(true) {
                System.out.println("Waiting for the client to join");
                String[] message = ServerHelper.receiveMessageFromClient(client, clientOis);
                System.out.println("Request Type : "+ message[0]);
                ServerHelper.processMessageFromClient(client, coordinator, type, message,Server.articleList,Server.dependencyList, clientOos, clientOis);
                //client.close();
              //  ServerHelper.receiveMapsfromCoordinator(coordinator);
            }
        } catch (Exception e) {
            System.out.println("Exception in ClientResponder");
//            switch(type){
//                case SEQUENTIAL:
//                    String[] message = ServerHelper.receiveMessageFromClient(client, in);
//                    try {
//                        ServerHelper.processMessageFromClient(client, coordinator, type, message,Server.articleList,Server.dependencyList,clientOos, clientOis);
//                    } catch (IOException | ClassNotFoundException e1) {
//                        e1.printStackTrace();
//                    }
//                case QUORUM:
//                case READ_YOUR_WRITE:
//                    String[] clientMessage = ServerHelper.receiveMessageFromClient(client, in);
//                    try {
//                        ServerHelper.processMessageFromClient(client, coordinator, type, clientMessage,Server.articleList,Server.dependencyList);
//                    } catch (IOException | ClassNotFoundException e1) {
//                        e1.printStackTrace();
//                    }
//                    ServerHelper.receiveMapsfromCoordinator(coordinator);
//            }

        }
// catch (ClassNotFoundException e) {
//            System.out.println("Couldn't close client socket");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }
}
