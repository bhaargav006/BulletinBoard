import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHelper {
    public static void sendMessageToServer(Socket socket, String message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message);
        System.out.println("Sent to Socket");
        output.close();
    }

    public static void receiveMessageFromServer(Socket socket) {
    }
}
