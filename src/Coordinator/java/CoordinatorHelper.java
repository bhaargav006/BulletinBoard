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

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> receiveMessageFromServer(SocketConnection socket, int ID) {
        //TODO has to be decoupled. It also processed the message right now. Change this to 2 functions for reusability.
        String result = null;
        int latestID;
        String[] messageReceived = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        ArrayList<Integer> childList = null;
        try {
            System.out.println("Server at: " + socket.getSocket().getLocalPort());
            ObjectInputStream objectInputStream = socket.getOis();//new ObjectInputStream(socket.getInputStream());
            int isWrite = (Integer) objectInputStream.readObject();
            dependencyList = (HashMap) objectInputStream.readObject();
            String message = (String) objectInputStream.readObject();

            // Gets the latest ID depending on whether it is post or reply.
            messageReceived = message.split("-");
            if (messageReceived.length >= 1) {
                latestID = getLatestID(socket.getSocket(), messageReceived[0], ID);
                result = latestID + " " + messageReceived[0];
            } else {
                latestID = getLatestID(socket.getSocket(), messageReceived[0], ID);
                result = latestID + " " + messageReceived[0] + " " + messageReceived[1];
                childList = dependencyList.get(messageReceived[0]);
                childList.add(latestID);
                dependencyList.put(Integer.parseInt(messageReceived[0]), childList);
            }
            Coordinator.ID = latestID;
            System.out.println("The latest ID is: " + latestID);

        } catch (IOException e) {
            System.out.println("Error while receiving message from Server");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't read object from server");
        }

        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<String, HashMap<Integer, ArrayList<Integer>>>(result, dependencyList);
        return pair;
    }

    public static int getLatestID(Socket socket, String message, int ID) {
        int latestID = ID + 1;
        return latestID;
    }

    public static void broadcastMessageToServers(String message, HashMap<Integer, ArrayList<Integer>> dependencyList) {
        try {
            for (SocketConnection server : Coordinator.serverSockets) {
                ObjectOutputStream objectOutputStream = server.getOos();
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

    public static void sendConsistencyTypeToServers(Socket socket, String consistency, ObjectOutputStream oos) {
        System.out.println("Sending consistency to Server");
        ObjectOutputStream objectOutputStream = null;
        try {
//            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream = oos;
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

    public static Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> getArticlesFromCoordinator(Socket coordinator) throws IOException, ClassNotFoundException {
        ArrayList<SocketConnection> listOfServerSockets = Coordinator.serverSockets;
        HashMap<Integer, ArrayList<Integer>> dependencyList = new HashMap<>();
        HashMap<Integer, String> articleList = new HashMap<>();
        for (SocketConnection socket : listOfServerSockets) {
            ObjectInputStream objectInputStream = socket.getOis();//new ObjectInputStream(socket.getInputStream());
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
            readServers.add(Coordinator.serverSockets.get(i).getSocket());
        }
        return readServers;
    }

    public static ArrayList<Socket> getWriteServers(Integer write) {
        ArrayList<Socket> writeServers = new ArrayList();
        for (int i = Coordinator.serverSockets.size() - 1; i >= (Coordinator.serverSockets.size() - write); i--) {
            writeServers.add(Coordinator.serverSockets.get(i).getSocket());
        }
        return writeServers;
    }
}

