import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ServerHelper {
    public static String[] receiveMessageFromClient(Socket socket){
        String[] ret = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String message = in.readUTF();
            ret = message.split(" ");
            System.out.println(ret);
            in.close();

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }

        return ret;
    }

    public static void processMessageFromClient(Socket socket, String[] message, HashMap<Integer, String> articleList, HashMap<Integer, List<Integer>> dependencyList) throws IOException {
        switch (message[0]){
            case "Read": sendArticlesToClient(articleList,dependencyList);break;
            /*TODO:
               1. Limit the number of TCP calls. It's VERY slow.
               2. Change this to process more than just one word for the operation.
                    Publish and Reply i -> invokes the same function, but reply with additional dependency.
               3.
             */
            case "Choose": sendChosenArticle(message[1], articleList);break;
            case "Publish":
            case "Reply":
                publishToCoordinator(socket, message, dependencyList);break;
            case "exit": socket.close(); break;
            default:
                System.out.println("Invalid");
        }
    }

    private static void sendChosenArticle(String ID, HashMap<Integer, String> articleList) {
        Integer articleID = Integer.getInteger(ID);
        String article = articleList.get(articleID);
        ///AAH how will I send this. Port of the client is unnecessarily needed. I create a scoket here and send it.
        //Client creates a server socket and recieves the message.
        //Let's discuss about this before I go further.
    }

    private static void publishToCoordinator(Socket socket1, String[] message, HashMap<Integer, List<Integer>> dependencyList) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost(), 9999);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(dependencyList);
        objectOutputStream.writeObject(message);
        System.out.println("Sent to Socket");
//        output.close();
    }

    private static void sendArticlesToClient(HashMap<Integer, String> articleList, HashMap<Integer, List<Integer>> dependencyList) {
    }

    public static void sendMessageToServer(Socket socket, String message) throws IOException {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message);
        System.out.println("Sent to Socket");
        output.close();
    }
}
