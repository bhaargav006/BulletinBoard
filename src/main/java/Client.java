import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("----******Welcome to the Bulletin Board******-----");
        System.out.println("Select any number to read more about the article");


        InetAddress host = InetAddress.getLocalHost();
        try {
            Socket socket = new Socket(host, 8000);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF("Read");
            System.out.println("Sent to Socket");
            output.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
