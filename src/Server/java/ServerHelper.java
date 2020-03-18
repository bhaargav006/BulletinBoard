import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerHelper {
    public static String[] receiveMessageFromClient(Socket socket){
        String[] ret = null;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String message = in.readUTF();
            ret = message.split(" ");
            System.out.println(ret[0]);
            //in.close();

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }

        return ret;
    }

    public static void processMessageFromClient(Socket socket, String[] message, HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList) throws IOException {
        switch (message[0]){
            case "Read": sendArticlesToClient(socket, articleList,dependencyList);break;
            /*TODO:
               1. Limit the number of TCP calls. It's VERY slow.
               2. Change this to process more than just one word for the operation.
                    Publish and Reply i -> invokes the same function, but reply with additional dependency.
               3.
             */
            case "Choose": sendChosenArticle(socket, message[1], articleList);break;
            case "Post":
            case "Reply":
                publishToCoordinator(socket, message);break;
            case "exit": socket.close(); break;
            default:
                System.out.println("Invalid");
        }
    }

    private static void sendChosenArticle(Socket socket, String ID, HashMap<Integer, String> articleList) {
        Integer articleID = Integer.getInteger(ID);
        String article = articleList.get(articleID);
        //Need to check this

        sendMessageToClient(socket,article);

    }

    private static void publishToCoordinator(Socket socket, String[] message) throws IOException {
        //Message format for post and reply needs to be set when client is sending it to server?
        //Currently I am parsing it as:
        //POST: <Insert-message>
        //REPLY: <ID-number> - <Insert-message>
        //We can change this if needed.
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeUTF(message.toString());
        System.out.println("Sent to Socket");
        output.close();
    }

    private static void sendArticlesToClient(Socket socket,HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        //Send the whole object to the client
        ObjectOutputStream output = null;
        try {
            articleList.put(0,"Dummy article");
            ArrayList<Integer> dummyList = new ArrayList<>();
            dummyList.add(0);
            dependencyList.put(1, dummyList);
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("I'm here");
            output.writeObject(articleList);
            output.writeObject(dependencyList);
            System.out.println("Sent to Client: Article: " +  articleList.get(0) + " Dependency: " + dependencyList.get(1));
            output.close();
        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }
    }

    public static void sendMessageToClient(Socket socket, String message) {

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(socket.getOutputStream());
            System.out.println("I'm here");
            output.writeUTF("Sending something back to the client");
            System.out.println("Sent to Client");
            output.close();
        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }

    }
}
