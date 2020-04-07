import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHelper {
    public static String[] receiveMessageFromClient(ObjectInputStream in) {
        String[] ret = null;
        try {

            ret = (String[]) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while receiving message from Client");
        }

        return ret;
    }

    /***
     * servers can get out of synch, to bring each replica up to date with each other,
     * we periodically call synch
     */
    public static void synch(int r) throws IOException, ClassNotFoundException {
        ArrayList<String> serverIPAndPort = CoordinatorHelper.getServerIPAndPort();
        ArrayList<String> nRPorts = new ArrayList<>();
        for(int i = 0; i < r; i++) {
            nRPorts.add(serverIPAndPort.get(i));
        }
        InetAddress chost = InetAddress.getLocalHost();
        //get max id from coordinator
        int cport = 8001; //set port of coordinator
        Socket coordinatorPort = new Socket(chost, cport);
        ObjectOutputStream coordout = new ObjectOutputStream(coordinatorPort.getOutputStream());
        ObjectInputStream coordin = new ObjectInputStream(coordinatorPort.getInputStream());
        coordout.writeObject(0);
        int latestId = (int)coordin.readObject();
        System.out.println("Latest ID is " + latestId);

        //get local maps from servers and create a global map with all the entries
        HashMap<Integer, String> globalArticleMap = new HashMap<>();
        HashMap<String, List<Integer>> missingArticleMapForEachServer = new HashMap<>();
        HashMap<String, ObjectOutputStream> outputStreamHashMap = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> globaldependencyMap = new HashMap<>();
        List<Socket> sockets = new ArrayList<>();
        for (String serv : serverIPAndPort) {
            Socket socket = new Socket(InetAddress.getLocalHost(), Integer.parseInt(serv));
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            sockets.add(socket);
            outputStreamHashMap.put(serv, oos);
            String[] msg = {"getArticlesMap"};
            oos.writeObject(msg);
            oos.reset();
            HashMap<Integer, String> localMap = (HashMap<Integer, String>) ois.readObject();
            HashMap<Integer, ArrayList<Integer>> dependencyList = (HashMap<Integer, ArrayList<Integer>>) ois.readObject();
            System.out.println("Sync: LocalMap " + localMap + " Dlist : " + dependencyList);

            for (int i = 1; i <= latestId; i++) {
                if (localMap.containsKey(i)) {
                    globalArticleMap.put(i, localMap.get(i));
                } else {
                    List<Integer> ids = missingArticleMapForEachServer.get(serv);
                    if (ids == null) {
                        List<Integer> id = new ArrayList<>();
                        id.add(i);
                        missingArticleMapForEachServer.put(serv, id);
                    } else {
                        ids.add(i);
                    }
                }
            }


            if(dependencyList!=null) {

                for (Map.Entry dependency : dependencyList.entrySet()) {
                    int arId = (int) dependency.getKey();
                    if (globaldependencyMap.containsKey(arId)) {
                        ArrayList<Integer> children = globaldependencyMap.get(arId);
                        ArrayList<Integer> localChild = (ArrayList<Integer>) dependency.getValue();
                        Collections.sort(children);
                        Collections.sort(localChild);
                        int lenOfChildren = children.size();
                        int lenOfLocalChildren = localChild.size();
                        ArrayList<Integer> resChild = new ArrayList<>();
                        int a = 0;
                        int b = 0;
                        while (a < lenOfChildren || b < lenOfLocalChildren) {
                            if (a >= lenOfChildren) {
                                resChild.add(localChild.get(b));
                                b++;
                                continue;
                            }
                            if (b >= lenOfChildren) {
                                resChild.add(children.get(a));
                                a++;
                                continue;
                            }
                            if (children.get(a).equals(localChild.get(b))) {
                                resChild.add(children.get(a));
                                a++;
                                b++;
                            } else if (children.get(a) < localChild.get(b)) {
                                resChild.add(children.get(a));
                                a++;
                            } else {
                                resChild.add(localChild.get(b));
                                b++;
                            }
                        }
                        globaldependencyMap.put(arId, resChild);
                    } else {
                        globaldependencyMap.put(arId, (ArrayList<Integer>) dependency.getValue());
                    }
                }
            }
        }

        for (String serv : serverIPAndPort) {
            List<Integer> ids = missingArticleMapForEachServer.get(serv);
            if (ids != null){
                for (int id : ids) {
                    String content = globalArticleMap.get(id);
                    ObjectOutputStream oos = outputStreamHashMap.get(serv);
                    String msg[] = {"SyncArticles", String.valueOf(id), content};
                    oos.writeObject(msg);
                }
            }
        }
        System.out.println(globaldependencyMap);
        if(globaldependencyMap!=null) {
            for (String serv : serverIPAndPort) {
                ObjectOutputStream oos = outputStreamHashMap.get(serv);
                for (Map.Entry e : globaldependencyMap.entrySet()) {
                    int arId = (int) e.getKey();
                    ArrayList<Integer> ar = (ArrayList<Integer>) e.getValue();
                    String arr = "";
                    for (int a : ar) {
                        arr += String.valueOf(a) + " ";
                    }
                    arr.trim();
                    String msg[] = {"SyncDependency", String.valueOf(arId), arr};
                    oos.writeObject(msg);
                }
            }
        }

        for (Socket s : sockets) {
            s.close();
        }

    }

    public static void updateArticleList(int articleId, String content) {
        Server.articleList.put(articleId, content);
    }

    public static void updateDependencyList(int articleId, String articles) {
        String[] ids = articles.split(" ");
        ArrayList<Integer> arr = new ArrayList<>();
        for (String id : ids)
            arr.add(Integer.parseInt(id));
        Server.dependencyList.put(articleId, arr);
    }

    public static void processMessageFromClient(SocketConnection client, SocketConnection coordinator, Consistency type, String[] message, HashMap<Integer,String> articleList, HashMap<Integer,ArrayList<Integer>> dependencyList, ObjectOutputStream clientOos, ObjectInputStream clientOis) throws IOException, ClassNotFoundException {
        System.out.println("Client's request is " + message[0]);
        switch (message[0]) {
            case "Read":
                sendArticlesToClient(type, articleList, dependencyList, clientOos, 0);
                break;

            case "Choose":

                sendChosenArticle(clientOos, type, message[1], articleList);
                break;

            case "Post":

            case "Reply":
                sendWriteToCoordinator(coordinator, message, dependencyList);
                break;

            case "getArticlesMap":
                sendArticlesToClient(type, articleList, dependencyList, clientOos, 1);
                break;
            case "Sync":
                break;
            case "SyncArticles":
                updateArticleList(Integer.parseInt(message[1]), message[2]);
                break;

            case "SyncDependency": {
                updateDependencyList(Integer.parseInt(message[1]), message[2]);
                break;
            }
            case "Update": {
                receiveMessagefromCoordinator(client);
                break;
            }
            default:
                System.out.println("Invalid");
        }
    }




    private static void sendChosenArticle(ObjectOutputStream client, Consistency type, String ID, HashMap<Integer, String> articleList) {

        Integer articleID = Integer.parseInt(ID);
        System.out.println("Article ID is " + articleID);

        if(type.equals(Consistency.QUORUM) || type.equals(Consistency.READ_YOUR_WRITE)) {
            try {
                ServerHelper.synch(CoordinatorHelper.getReadAndWriteServers().get("Read"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        String article = articleList.get(articleID);
        if (article == null || article == "")
            article = "Invalid article ID. There is no such article";

        sendMessageToClient(client, article);

    }

    private static void sendWriteToCoordinator(SocketConnection coordinator, String[] message, HashMap<Integer, ArrayList<Integer>> dependencyList)  {

        int flag = 0;
        if(message[0].equals("Reply")) {
            flag =1;
        }
        StringBuilder sb = getStringBuilder(message);
        System.out.println(sb.toString());
        try {
            ObjectOutputStream objectOutputStream = coordinator.getOos();//new ObjectOutputStream(coordinator.getOutputStream());

//            objectOutputStream.writeObject(flag);
            objectOutputStream.writeObject(dependencyList);
            objectOutputStream.writeObject(sb.toString());
            System.out.println("Sent to Coordinator");
            //receiveMessagefromCoordinator(coordinator);

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the Coordinator");

        }
    }

    //TODO change local copy of server from coordinator

    private static void sendReadToCoordinator(SocketConnection coordinator, String[] message)  {

        int flag = 0;
        if(message[0] == "Read") {
            flag = 1;
        }
        StringBuilder sb = getStringBuilder(message);
        System.out.println(sb.toString());
        try {
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(coordinator.getOutputStream());
            ObjectOutputStream objectOutputStream= coordinator.getOos();
            objectOutputStream.writeObject(flag);
            objectOutputStream.writeObject(message);
            System.out.println("Sent to Coordinator");
            //receiveMessagefromCoordinator(coordinator);

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the Coordinator");

        }
    }

    private static StringBuilder getStringBuilder(String[] message) {

        //POST: <Insert-message>
        //REPLY: <ID-number> - <Insert-message>

        StringBuilder sb = new StringBuilder("");
        int i = 1;
        if (message[0].equalsIgnoreCase("Reply")) {
            sb.append(message[1]);
            sb.append("-");
            i++;
        }
        while (i < message.length) {
            sb.append(message[i]);
            i++;
            sb.append(" ");
        }
        return sb;
    }

    private static void sendArticlesToClient(Consistency type, HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList,ObjectOutputStream cloos, int flag) {
        //Send the whole object to the client
        ObjectOutputStream output = null;
        try {

            /**
             * Get updated articleList after contacting Nr servers
             */
            if(flag==0 && (type.equals(Consistency.QUORUM) || type.equals(Consistency.READ_YOUR_WRITE)) ) {
                int r = CoordinatorHelper.getReadAndWriteServers().get("Read");
                ServerHelper.synch(r);
            }
            System.out.println("Sync worked");
            output = cloos;
            output.writeObject(Server.articleList);
            output.writeObject(Server.dependencyList);
            output.reset();
            System.out.println("Sent to Client: Article: " + Server.articleList + " Dependency: " + Server.dependencyList);

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can't send message back to the client");
        }
    }


    public static void sendMessageToClient (ObjectOutputStream socket, String message){
        //Send the string message to the client
        try {
            socket.writeObject(message);
            socket.reset();
            System.out.println("Sent the article to Client: " + message);

        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }

    }


    public static void receiveMessagefromCoordinator (SocketConnection socket){

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = socket.getOis();
            String readMesage = (String) objectInputStream.readObject();

            String dependency = (String) objectInputStream.readObject();
            HashMap<Integer, ArrayList<Integer>> dependencyList = convertToMap(dependency);

            System.out.println("Message from Coordiantor" + readMesage);
            String [] rec = readMesage.split(" ");
            String msg = "";
            for(int i = 1; i < rec.length; i++) {
                msg += rec[i] + " ";
            }
            Server.articleList.put(Integer.parseInt(rec[0]), msg.trim());

            System.out.println("Message from coordinator::: "+dependencyList);
            if(dependencyList!=null)
                Server.dependencyList.putAll(dependencyList);
            System.out.println("Updated Dependency List "+Server.dependencyList);
        } catch (IOException e) {
            System.out.println("Can't read from coordinator");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't convert the message received from coordinator");
        }

        //How am I suppose to update the dependency list and articleList? I am Garima FYI
//        updateArticleAndDependencyList()
    }

    public static void receiveMapsfromCoordinator (SocketConnection coordinator){
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream =  coordinator.getOis();//new ObjectInputStream(coordinator.getInputStream());
            HashMap<Integer, String> articleList = (HashMap) objectInputStream.readObject();
            HashMap<Integer, ArrayList<Integer>> dependencyList = (HashMap) objectInputStream.readObject();
            ClientHelper.readArticles(articleList, dependencyList);
        } catch (IOException e) {
            System.out.println("Can't read from coordinator");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't convert the message received from coordinator");
        }
    }

    public static Consistency getConsistencyType (SocketConnection coordinator){
        ObjectInputStream objectInputStream;
        System.out.println("Waiting to receive message from coordinator");
        try {
//            objectInputStream = new ObjectInputStream(coordinator.getInputStream());
            objectInputStream = coordinator.getOis();
            String readMessage = (String) objectInputStream.readObject();
            return Enum.valueOf(Consistency.class, readMessage);
        } catch (IOException e) {
            System.out.println("Can't read from coordinator");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't convert the message received from coordinator");
        }
        return Consistency.ERROR;
    }
    public static HashMap<Integer, ArrayList<Integer>>  convertToMap(String dep) {
        HashMap<Integer, ArrayList<Integer>> ans = new HashMap<>();
        if(dep.length() ==0) return null;
        String [] entries = dep.split(";");
        for(String e: entries) {
            if(e.length() == 0) continue;
            ArrayList<Integer> temp = new ArrayList<>();
            String [] kv = e.split("=");
            String [] values = kv[1].split(",");
            int key = Integer.parseInt(kv[0]);
            for(String v: values){
                if(v.length() == 0) continue;
                temp.add(Integer.parseInt(v));
            }
            ans.put(key,temp);
        }
        return ans;
    }


}