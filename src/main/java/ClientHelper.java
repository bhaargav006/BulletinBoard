import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHelper {
    public static void sendMessageToServer(Socket socket, String message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message);
        System.out.println("Sent to Socket");
        //output.close();
    }

    public static String receiveMessageFromServer(Socket socket) {
//        String ret = null;
//        try {
//            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//            ret = in.readUTF();
//            System.out.println(ret);
//            in.close();
//
//        } catch (IOException e) {
//            System.out.println("Error while receiving message from Client");
//        }
        HashMap<Integer, String> ret = null;
        HashMap<Integer, ArrayList<Integer>> ret1 = null;
        try {
            System.out.println("I'm here too");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ret = (HashMap) in.readObject();
            ret1 = (HashMap) in.readObject();
            System.out.println(ret.get(0));
            System.out.println(ret1.get(1));
            in.close();

        } catch (IOException e1) {
            System.out.println("Error while receiving message from Server");
        } catch ( ClassNotFoundException e){
            System.out.println("Another Exception this time");
        }

        return " ";
    }
}
