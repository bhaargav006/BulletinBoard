import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("----******Welcome to the Bulletin Board******-----");
        System.out.println("Select any number to read more about the article");

        //TODO: Change the data sent based on the input from the user
        InetAddress host = InetAddress.getLocalHost();
        try {
            Socket socket = new Socket(host, 8000);
            String message = "Choose 3";
            ClientHelper.sendMessageToServer(socket,message);
            ///AAAAH TCP bad idea. need to create server socket here, to listen from the server.
            //System.out.println(ClientHelper.recieveMessageFromServer(socket));
            socket.close();

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the server");
        }

    }
}
