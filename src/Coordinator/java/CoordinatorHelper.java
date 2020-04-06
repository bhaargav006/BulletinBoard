import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CoordinatorHelper {

    public static ArrayList<String> getServerIPAndPort() throws IOException {
        ArrayList<String> listOfServers = new ArrayList<>();
        File file = new File("serverList.properties");
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

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> receiveMessageFromServer(SocketConnection socket) {
        String message = null;
        HashMap<Integer, ArrayList<Integer>> dependencyList = null;
        try {
            System.out.println("Server at: " + socket.getSocket().getLocalPort());
            ObjectInputStream objectInputStream = socket.getOis();//new ObjectInputStream(socket.getInputStream());


            /**
             * Read request in the case of quorum
             */
//            if(requestFlag<2){
//                message = (String) objectInputStream.readObject();
//                return new Pair<>(message, null);
//            }

            dependencyList = (HashMap) objectInputStream.readObject();
            message = (String) objectInputStream.readObject();
//            System.out.println("Ffrom server:" + message + " " + dependencyList);

        } catch (IOException | ClassNotFoundException e1){
            System.out.println("Couldn't receive message from server");
        }
        return new Pair<>(message, dependencyList);
    }

    public static Pair<String, HashMap<Integer, ArrayList<Integer>>> processMessageReceivedFromServer(Socket server, String message, HashMap<Integer, ArrayList<Integer>> dependencyList, int id) {
        // Gets the latest ID depending on whether it is post or reply.
        String[] messageReceived = null;
        int latestID;
        String result = "";
        ArrayList<Integer> childList = new ArrayList<>();
        messageReceived = message.split("-");
//        System.out.println(message);
        if (messageReceived.length < 2) {
            latestID = getLatestID(server, messageReceived[0],id);
            result = latestID + " " + messageReceived[0];
        } else {
            latestID = getLatestID(server, messageReceived[0], id);
            result = latestID + " " + messageReceived[1];
            childList = dependencyList.get(Integer.parseInt(messageReceived[0]));
            if(childList != null)
                childList.add(latestID);
            else {
                childList = new ArrayList<>();
                childList.add(latestID);
            }
            dependencyList.put(Integer.parseInt(messageReceived[0]), childList);
        }
//        System.out.println(dependencyList);
        Coordinator.ID = latestID;
        System.out.println("The latest ID is: " + latestID);
        Pair<String, HashMap<Integer, ArrayList<Integer>>> pair = new Pair<String, HashMap<Integer, ArrayList<Integer>>>(result, dependencyList);
        return pair;
    }

    public static int getLatestID(Socket socket, String message, int ID) {
        int latestID = ID + 1;
        return latestID;
    }

    public static void broadcastMessageToServers(String message, HashMap<Integer, ArrayList<Integer>> dependencyList, ArrayList<SocketConnection> writeServers) {
        System.out.println(dependencyList);
        String dl = convertToString(dependencyList);
        try {
            for (SocketConnection server : writeServers) {
                SocketConnection temp = new SocketConnection(8000);
                ObjectOutputStream objectOutputStream = temp.getOos();
                String[] tempMsg= {"Update"};
                objectOutputStream.writeObject(tempMsg);
                objectOutputStream.writeObject(message);
                objectOutputStream.writeObject(dl);
                objectOutputStream.reset();
                System.out.println("Sent to Socket");
            }
        } catch (IOException e) {
            System.out.println("Problem broadcasting to servers from coordinator");
        }

    }

    public static String convertToString(HashMap<Integer, ArrayList<Integer>> a) {
        String ans = "";
        for(Map.Entry e: a.entrySet()) {
            ans += String.valueOf(e.getKey());
            ans += "=";
            ArrayList<Integer> res = (ArrayList<Integer>) e.getValue();
            for(int r: res) {
                ans += String.valueOf(r +",");
            }
            ans += ";";
        }
        return ans;
    }

    public static ConcurrentHashMap<String, Integer> getReadAndWriteServers() throws IOException {
        ConcurrentHashMap<String, Integer> readWriteServers = new ConcurrentHashMap<String, Integer>();
        File file = new File("serverList.properties");
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
                return Consistency.EXIT;
            default:
                System.out.println("Invalid Input");
                return Consistency.ERROR;
        }
    }


    public static Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> getArticlesFromCoordinator(Socket coordinator, ArrayList<SocketConnection> serverSockets) throws IOException, ClassNotFoundException {
        ArrayList<SocketConnection> listOfServerSockets = serverSockets;
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

    public static ArrayList<SocketConnection> getReadServers(Integer read) {
        ArrayList<SocketConnection> readServers = new ArrayList();
        for (int i = 0; i < read; i++) {
            readServers.add(Coordinator.serverSockets.get(i));
        }
        return readServers;
    }

    public static ArrayList<SocketConnection> getWriteServers(Integer write) {
        ArrayList<SocketConnection> writeServers = new ArrayList();
        for (int i = Coordinator.serverSockets.size() - 1; i >= (Coordinator.serverSockets.size() - write); i--) {
            writeServers.add(Coordinator.serverSockets.get(i));
        }
        return writeServers;
    }

    public static void sendArticlesToServers(SocketConnection server, Pair<HashMap<Integer, String>, HashMap<Integer, ArrayList<Integer>>> globalPair) {
        try {
            ObjectOutputStream objectOutputStream = server.getOos();//new ObjectOutputStream(server.getOutputStream());
            objectOutputStream.writeObject(globalPair);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

