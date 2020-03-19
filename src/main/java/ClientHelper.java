import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHelper {
    public static void sendMessageToServer(Socket socket, String[] message, int flag) throws IOException {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.writeObject(message);
        System.out.println("Sent to Socket");
        if(flag==0)
            output.close();
        if(flag==1)
            receiveMessageFromServer(socket, 0);
        if(flag==2)
            receiveMessageFromServer(socket, 1);
    }

    public static void receiveMessageFromServer(Socket socket, int flag) {

        HashMap<Integer, String> articleList = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        String choose = null;
        try {
            System.out.println("I'm here too");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            if(flag==0){
                choose= (String) in.readObject();
                System.out.println(choose);
            }
            else if(flag==1){
                articleList = (HashMap) in.readObject();
                dependencyList = (HashMap) in.readObject();
                System.out.println("Got articleList and dependencyList from the server");
                readArticles(articleList, dependencyList);
            }

            in.close();

        } catch (IOException e1) {
            System.out.println("Error while receiving message from Server");
        } catch ( ClassNotFoundException e){
            System.out.println("Another Exception this time");
        }

    }

    private static void readArticles(HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        System.out.println("Print read stuff");
        //Do a DFS
    }

    public static void processMessage(Socket socket, String message) throws IOException {
        String[] messageList = message.split(" ");
        switch (messageList[0]){
            case "Read": sendMessageToServer(socket, messageList, 2);break;
            case "Choose": sendMessageToServer(socket, messageList, 1);break;
            case "Post":
            case "Reply":
                sendMessageToServer(socket, messageList, 0);break;
            case "exit":
                System.out.println("Exiting from the program");
                socket.close(); break;
            default:
                System.out.println("Invalid");
        }
    }
}
