import javafx.util.Pair;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class CoordinatorHelper {

    public static ArrayList<String> getServerIPAndPort() throws IOException {
        ArrayList<String> listOfServers = new ArrayList<>();
        File file = new File("serverList.properties");
        FileInputStream fileInputStream = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(fileInputStream);

        while(prop.keys().hasMoreElements()){
            String server = (String)prop.keys().nextElement();
            listOfServers.add(server.split(":")[1]);
        }

        return listOfServers;
    }

    public static Pair<String, HashMap<Integer, Integer>> receiveMessageFromServer(Socket socket, HashMap<String, String> serverMessageQueue, int ID) throws ClassNotFoundException {
        String result = null;
        int latestID = 0;

        HashMap<Integer, Integer> dependencyList = null;

        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            dependencyList = (HashMap) objectInputStream.readObject();
            String message = (String) objectInputStream.readObject();
            // Gets the latest ID depending on whether it is post or reply.
            String[] messageToSend = message.split("-");
            if(messageToSend.length > 1)
                latestID = getLatestID(socket, messageToSend[1], ID);
            else
                latestID = getLatestID(socket, messageToSend[0], ID);
            System.out.println("The latest ID is: " + latestID);

            result = latestID + messageToSend[1];
            in.close();

        } catch (IOException e) {
            System.out.println("Error while receiving message from Client");
        }
        Pair<String, HashMap<Integer, Integer>> pair = new Pair<String, HashMap<Integer, Integer>>(result, dependencyList);
        return pair;
    }

    public static int getLatestID(Socket socket, String message, int ID){
        return ID += ID;
    }

    public static void broadcastMessageToServers(Socket socket, String message, HashMap<Integer, Integer> dependencyList) throws IOException {
        ArrayList<String> listOfServers = getServerIPAndPort();
        Enumeration enumeration = Collections.enumeration(listOfServers);
        while(enumeration.hasMoreElements()){
            int port = (int)enumeration.nextElement();
            Socket serverSocket = new Socket(InetAddress.getLocalHost(), port);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.writeObject(dependencyList);
            System.out.println("Sent to Socket");
            objectOutputStream.close();
            serverSocket.close();
        }
    }
}
