import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHelper {
    public static void sendMessageToServer(SocketConnection socket, String[] message, int flag) throws IOException {
        socket.getOos().writeObject(message);
        System.out.println("Sent to Socket");
        if(flag==1)
            receiveMessageFromServer(socket, 0);
        if(flag==2)
            receiveMessageFromServer(socket, 1);
    }

    public static void receiveMessageFromServer(SocketConnection socket, int flag) {

        HashMap<Integer, String> articleList = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        String choose = null;
        try {
            System.out.println("I'm here too");
            //ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectInputStream in = socket.getOis();
            System.out.println("falg" + flag);
            if(flag==0){
                choose= (String) in.readObject();
                System.out.println(choose);
            }
            else if(flag==1){
                articleList = (HashMap) in.readObject();
                dependencyList = (HashMap) in.readObject();
                System.out.println("Got articleList and dependencyList from the server" + articleList.size() + dependencyList.size());
                readArticles(articleList, dependencyList);
            }

            //in.close();

        } catch (IOException e1) {
            System.out.println("Error while receiving message from Server");
        } catch ( ClassNotFoundException e){
            System.out.println("Another Exception this time");
        }

    }

    public static void readArticles(HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        int i = 1;
        boolean[] visitedArray = new boolean[articleList.size()];
        createString(articleList, dependencyList,visitedArray, i,0);
        //Do a DFS
    }


    private static void createString(HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList, boolean[] visitedArray, int index, int spaces) {
        if(index > articleList.size()){
            return;
        }
        if(visitedArray[index - 1] == false){
            System.out.println(index +". " +articleList.get(index));
        }
        ArrayList<Integer> childList = dependencyList.get(index);
        visitedArray[index - 1] = true;
        if(childList == null || childList.size() == 0){
            createString(articleList, dependencyList,visitedArray, index + 1, spaces);
        }
        else{
           // System.out.print("\t");
            for(int i = 0; i < childList.size(); i++){
                if(visitedArray[childList.get(i) - 1] == false){
                    for(int t = 0; t <=git  spaces; t++) System.out.print("\t");
                    System.out.print(childList.get(i) +". " +articleList.get(childList.get(i)));
                    System.out.println();
                    visitedArray[childList.get(i) - 1] = true;
                    if(dependencyList.get(childList.get(i)) == null) continue;
                    createString(articleList,dependencyList,visitedArray,childList.get(i), spaces+1);

                }
            }
            createString(articleList, dependencyList,visitedArray, index + 1, spaces);
        }
    }

    public static void processMessage(SocketConnection socket, String message ) throws IOException {
        String[] messageList = message.split(" ");
        switch (messageList[0]){
            case "Read": sendMessageToServer(socket, messageList, 2);break;
            case "Choose": sendMessageToServer(socket, messageList, 1);break;
            case "Post": //sendMessageToServer(socket,messageList,0);
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
