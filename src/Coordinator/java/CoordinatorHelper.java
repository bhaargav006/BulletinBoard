import javafx.util.Pair;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class CoordinatorHelper {

    public static ArrayList<String> getServerIPAndPort() throws IOException {
        ArrayList<String> listOfServers = new ArrayList<>();
        File file = new File("src/serverList.properties");
        FileInputStream fileInputStream = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(fileInputStream);
        Enumeration enumeration = prop.keys();
        listOfServers.add(prop.getProperty("server1").split(":")[1]);
        listOfServers.add(prop.getProperty("server2").split(":")[1]);
        listOfServers.add(prop.getProperty("server3").split(":")[1]);
        listOfServers.add(prop.getProperty("server4").split(":")[1]);
//        while(enumeration.hasMoreElements()){
//            String server = prop.getProperty((String) prop.keys().nextElement());
//            listOfServers.add(server.split(":")[1]);
//        }

        return listOfServers;
    }

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> receiveMessageFromServer(Socket socket, HashMap<String, String> serverMessageQueue, int ID) throws ClassNotFoundException {
        String result = null;
        int latestID;
        String[] messageToSend = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        ArrayList<Integer> childList = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            dependencyList = (HashMap) objectInputStream.readObject();
            String message = (String) objectInputStream.readObject();
            // Gets the latest ID depending on whether it is post or reply.
            messageToSend = message.split("-");
            if(messageToSend.length >= 1){
                latestID = getLatestID(socket, messageToSend[0], ID);
                result = latestID + " " + messageToSend[0];
            }
            else{
                latestID = getLatestID(socket, messageToSend[0], ID);
                result = latestID + " " + messageToSend[0] + " " + messageToSend[1];
                childList = dependencyList.get(messageToSend[0]);
                childList.add(latestID);
                dependencyList.put(Integer.parseInt(messageToSend[0]), childList);
            }

            System.out.println("The latest ID is: " + latestID);
//            objectInputStream.close();

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }

        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<String, HashMap<Integer, ArrayList<Integer>>>(result, dependencyList);
        return pair;
    }

    public static int getLatestID(Socket socket, String message, int ID){
        int latestID = ID + 1;
        return latestID;
    }

    public static void broadcastMessageToServers(Socket socket, String message, HashMap<Integer, ArrayList<Integer>> dependencyList) throws IOException {
        ArrayList<String> listOfServers = getServerIPAndPort();
        Enumeration enumeration = Collections.enumeration(listOfServers);
        while(enumeration.hasMoreElements()){
            int port = Integer.parseInt((String)enumeration.nextElement());
//            Socket serverSocket = new Socket(InetAddress.getLocalHost(), port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.writeObject(dependencyList);
            System.out.println("Sent to Socket");
            objectOutputStream.close();
            socket.close();
        }
    }
}
