import javafx.util.Pair;

import java.io.*;
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

        return listOfServers;
    }

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> receiveMessageFromServer(Socket socket) {
        String message = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        try {
            System.out.println("Server at: " + socket.getLocalPort());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            int isWrite = (Integer) objectInputStream.readObject();
            dependencyList = (HashMap) objectInputStream.readObject();
            message = (String) objectInputStream.readObject();

        } catch (IOException | ClassNotFoundException e1){
            System.out.println("Couldn't receive message from server");
        }
        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<String, HashMap<Integer, ArrayList<Integer>>>(message, dependencyList);
        return pair;
    }

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> processMessageReceivedFromServer(Socket server, Consistency type, String message, HashMap<Integer, ArrayList<Integer>> dependencyList, int id) {
        // Gets the latest ID depending on whether it is post or reply.
        String[] messageReceived = null;
        int latestID;
        String result = "";
        ArrayList<Integer> childList = null;
        messageReceived = message.split("-");
        if (messageReceived.length >= 1) {
            latestID = getLatestID(server, messageReceived[0],id);
            result = latestID + " " + messageReceived[0];
        } else {
            latestID = getLatestID(server, messageReceived[0], id);
            result = latestID + " " + messageReceived[0] + " " + messageReceived[1];
            childList = dependencyList.get(messageReceived[0]);
            childList.add(latestID);
            dependencyList.put(Integer.parseInt(messageReceived[0]), childList);
        }

        System.out.println("The latest ID is: " + latestID);
        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<String, HashMap<Integer, ArrayList<Integer>>>(result, dependencyList);
        return pair;
    }

    public static int getLatestID(Socket socket, String message, int ID) {
        int latestID = ID + 1;
        return latestID;
    }

    public static void broadcastMessageToServers(String message, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        try {
            for (Socket server : Coordinator.serverSockets) {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.writeObject(dependencyList);
                System.out.println("Sent to Socket");
            }
        } catch (IOException e) {
            System.out.println("Problem broadcasting to servers from coordinator");
        }

    }

    public static HashMap<String, Integer> getReadAndWriteServers() throws IOException {
        HashMap<String, Integer> readWriteServers = new HashMap<String, Integer>();
        File file = new File("src/serverList.properties");
        FileInputStream fileInputStream = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(fileInputStream);
        Enumeration enumeration = prop.keys();
        readWriteServers.put("Read", Integer.parseInt(prop.getProperty("numberOfReadServers")));
        readWriteServers.put("Write", Integer.parseInt(prop.getProperty("numberOfWriteServers")));

        return readWriteServers;
    }

    public static boolean validReadWriteServerValues(Integer read, Integer write, Integer N) {
        if ((read + write > N) && (write > N / 2))
            return true;
        return false;
    }

    public static void sendConsistencyTypeToServers(Socket socket, String consistency) {
        System.out.println("Sending consistency to Server");
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(consistency);
        } catch (IOException e) {
            System.out.println("Couldn't send Consistency type to server at port " + socket.getPort());
        }

    }

    public static Consistency getConsistencyType(String consistency) {
        switch (consistency) {
            case "Seq":
                return Consistency.SEQUENTIAL;
            case "Quo":
                return Consistency.QUORUM;
            case "RYW":
                return Consistency.READ_YOUR_WRITE;
            case "Exit":
                System.out.println("Bye Bye");
                return Consistency.ERROR;
            default:
                System.out.println("Invalid Input");
                return Consistency.EXIT;
        }
    }

    public static Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> getArticlesFromCoordinator(Socket coordinator, ArrayList<Socket> serverSockets) throws IOException, ClassNotFoundException {
        ArrayList<Socket> listOfServerSockets = serverSockets;
        HashMap<Integer, ArrayList<Integer>> dependencyList = new HashMap<>();
        HashMap<Integer, String> articleList = new HashMap<>();
        for (Socket socket : listOfServerSockets) {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            HashMap<Integer, String> serverArticleList = (HashMap) objectInputStream.readObject();
            HashMap<Integer, ArrayList<Integer>> serverDepenedencyList = (HashMap) objectInputStream.readObject();

            HashSet<Integer> keys = new HashSet<>(serverArticleList.keySet());
            HashSet<Integer> dependencyKeys = new HashSet<>(serverDepenedencyList.keySet());

            while (keys.iterator().hasNext()) {
                if (articleList.size() == Coordinator.ID) {
                    break;
                }
                if (!articleList.containsKey(keys.iterator().next())) {
                    articleList.put(keys.iterator().next(), serverArticleList.get(keys.iterator().next()));
                }
            }

            while (dependencyKeys.iterator().hasNext()) {
                if (!dependencyList.containsKey(dependencyKeys.iterator().next())) {
                    dependencyList.put(dependencyKeys.iterator().next(), serverDepenedencyList.get(dependencyKeys.iterator().next()));
                }
                ArrayList<Integer> serverChildList = serverDepenedencyList.get(dependencyKeys.iterator().next());
                ArrayList<Integer> childList = dependencyList.get(dependencyKeys.iterator().next());
                for (int value : serverChildList) {
                    if (!childList.contains(value)) {
                        childList.add(value);
                    }
                }
                dependencyList.put(dependencyKeys.iterator().next(), childList);

            }
        }
        Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<>(articleList, dependencyList);
        return pair;
    }

    public static ArrayList<Socket> getReadServers(Integer read) {
        ArrayList<Socket> readServers = new ArrayList();
        for (int i = 0; i < read; i++) {
            readServers.add(Coordinator.serverSockets.get(i));
        }
        return readServers;
    }

    public static ArrayList<Socket> getWriteServers(Integer write) {
        ArrayList<Socket> writeServers = new ArrayList();
        for (int i = Coordinator.serverSockets.size() - 1; i >= (Coordinator.serverSockets.size() - write); i--) {
            writeServers.add(Coordinator.serverSockets.get(i));
        }
        return writeServers;
    }

    public static void sendArticlesToServers(Socket server, Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> globalPair) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            objectOutputStream.writeObject(globalPair);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

