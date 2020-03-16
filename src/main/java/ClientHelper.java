import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHelper {
    public static void sendMessageToServer(Socket socket, String message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message);
        System.out.println("Sent to Socket");
        //output.close();
    }

    public static String receiveMessageFromServer(Socket socket) {
        String ret = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            ret = in.readUTF();
            System.out.println(ret);
            in.close();

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }

        return ret;
    }
}
