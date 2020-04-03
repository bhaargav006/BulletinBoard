import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerHelper {
    public static String[] receiveMessageFromClient(Socket socket){
        String[] ret = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ret = (String[])in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while receiving message from Client");
        }

        return ret;
    }


    public static void processMessageFromClient(Socket client, Socket coordinator, String[] message, HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList)  {
       System.out.println("Client's request is " + message[0]);
        switch (message[0]){
            case "Read": sendArticlesToClient(client, articleList,dependencyList);break;
            case "Choose": sendChosenArticle(client, message[1], articleList);break;
            case "Post":
            case "Reply":
                publishToCoordinator(coordinator, message, dependencyList);break;
            default:
                System.out.println("Invalid");
        }
    }

    private static void sendChosenArticle(Socket socket, String ID, HashMap<Integer, String> articleList) {
        articleList.put(0,"Dummy article");
        Integer articleID = Integer.parseInt(ID);
        System.out.println("Article ID is " + articleID);
        String article = articleList.get(articleID);
        if(article == null || article == "")
            article = "Invalid article ID. There is no such article";
        sendMessageToClient(socket,article);

    }

    private static void publishToCoordinator(Socket coordinator, String[] message, HashMap<Integer, ArrayList<Integer>> dependencyList)  {

        //POST: <Insert-message>
        //REPLY: <ID-number> - <Insert-message>

        StringBuilder sb = getStringBuilder(message);
        System.out.println(sb.toString());
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(coordinator.getOutputStream());
            objectOutputStream.writeObject(dependencyList);
            objectOutputStream.writeObject(sb.toString());
            System.out.println("Sent to Coordinator");
            receiveMessagefromCoordinator(coordinator);

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the Coordinator");

        }

    }

    private static StringBuilder getStringBuilder(String[] message) {
        StringBuilder sb = new StringBuilder("");
        int i=1;
        if(message[0].equalsIgnoreCase("Reply")){
            sb.append(message[1]);
            sb.append("-");
            i++;
        }
        while(i<message.length){
            sb.append(message[i]);
            i++;
            sb.append(" ");
        }
        return sb;
    }

    private static void sendArticlesToClient(Socket client, HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        //Send the whole object to the client
        ObjectOutputStream output = null;
        try {
            articleList.put(0,"Dummy article");
            ArrayList<Integer> dummyList = new ArrayList<>();
            dummyList.add(0);
            dependencyList.put(1, dummyList);
            output = new ObjectOutputStream(client.getOutputStream());
            output.writeObject(articleList);
            output.writeObject(dependencyList);
            System.out.println("Sent to Client: Article: " +  articleList.get(0) + " Dependency: " + dependencyList.get(1));
            output.close();
        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }
    }

    public static void sendMessageToClient(Socket socket, String message) {
        //Send the string message to the client
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(message);
            System.out.println("Sent the article to Client");
            output.close();
        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }

    }

    public static void receiveMessagefromCoordinator(Socket socket) {
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            String readMessage = (String) objectInputStream.readObject();
            HashMap<Integer, Integer> dependencyList = (HashMap) objectInputStream.readObject();
        } catch (IOException e) {
            System.out.println("Can't read from coordinator");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't convert the message received from coordinator");
        }

        //How am I suppose to update the dependency list and articleList?
//        updateArticleAndDependencyList()
    }
}
