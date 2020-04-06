import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerHelper {
    public static String[] receiveMessageFromClient(Socket socket,ObjectInputStream in) {
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
    public static void synch() throws IOException, ClassNotFoundException {
        ArrayList<String> serverIPAndPort = CoordinatorHelper.getServerIPAndPort();
        InetAddress chost = InetAddress.getLocalHost();
        //get max id from coordinator
        int cport = 8001; //set port of coordinator
        Socket coordinatorPort = new Socket(chost, cport);
        ObjectOutputStream coos = new ObjectOutputStream(coordinatorPort.getOutputStream());
        ObjectInputStream cois = new ObjectInputStream(coordinatorPort.getInputStream());
        coos.writeUTF("getLatestId");
        int latestId = Integer.parseInt(cois.readUTF());

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
            HashMap<Integer, String> localMap = (HashMap<Integer, String>) ois.readObject();
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

            String[] msgForDep = {"getDependencyMap"};
            oos.writeObject(msgForDep);
            HashMap<Integer, ArrayList<Integer>> dependencyList = (HashMap<Integer, ArrayList<Integer>>) ois.readObject();
            for (Map.Entry dependency : dependencyList.entrySet()) {
                int arId = (int) dependency.getKey();
                if (globaldependencyMap.containsKey(arId)) {
                    ArrayList<Integer> children = globaldependencyMap.get(arId);
                    ArrayList<Integer> localChild = (ArrayList<Integer>) dependency.getValue();
                    int lenOfChildren = children.size();
                    int lenOfLocalChildren = localChild.size();
                    ArrayList<Integer> resChild = new ArrayList<>();
                    int a = 0;
                    int b = 0;
                    while (a < lenOfChildren || b < lenOfLocalChildren) {
                        if (a >= lenOfChildren) {
                            resChild.add(b);
                            b++;
                            continue;
                        }
                        if (b >= lenOfChildren) {
                            resChild.add(a);
                            a++;
                            continue;
                        }
                        if (children.get(a).equals(localChild.get(b))) {
                            resChild.add(a);
                            a++;
                            b++;
                        } else if (children.get(a) < localChild.get(b)) {
                            resChild.add(a);
                            a++;
                        } else {
                            resChild.add(b);
                            b++;
                        }
                    }
                    globaldependencyMap.put(arId, resChild);
                } else {
                    globaldependencyMap.put(arId, (ArrayList<Integer>) dependency.getValue());
                }
            }
        }

        for (String serv : serverIPAndPort) {
            List<Integer> ids = missingArticleMapForEachServer.get(serv);
            for (int id : ids) {
                String content = globalArticleMap.get(id);
                ObjectOutputStream oos = outputStreamHashMap.get(serv);
                String msg[] = {"SyncArticles", String.valueOf(id), content};
                oos.writeObject(msg);
            }
        }
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

    public static void processMessageFromClient(Socket client, SocketConnection coordinator, Consistency type, String[] message, HashMap<Integer,String> articleList, HashMap<Integer,ArrayList<Integer>> dependencyList, ObjectOutputStream clientOos, ObjectInputStream clientOis) throws IOException, ClassNotFoundException {
        System.out.println("Client's request is " + message[0]);
        switch (message[0]) {
            case "Read":
                if (type.equals(Consistency.READ_YOUR_WRITE)) {

                    CoordinatorHelper.getArticlesFromCoordinator(coordinator.getSocket(), Coordinator.serverSockets);
                }
                else sendArticlesToClient(client, coordinator, type, articleList, dependencyList, message, clientOis, clientOos);
                break;

            case "Choose":
            //    sendChosenArticle(client, coordinator, message[1], articleList);
                break;

            case "Post":

            case "Reply":
                sendWriteToCoordinator(coordinator, message, dependencyList);

                break;

            case "getLocalMap":
                // sendArticlesToClient(socket, articleList,dependencyList);break;
            case "Sync":
            case "SyncArticles":
                updateArticleList(Integer.parseInt(message[1]), message[2]);
                // sendArticlesToClient(socket, articleList,dependencyList);break;
            case "SyncDependency":
                //update the local map
                updateDependencyList(Integer.parseInt(message[1]), message[2]);

            default:
                System.out.println("Invalid");
        }
    }



    private static void sendChosenArticle(Socket client, Socket coordinator, String ID, HashMap<Integer, String> articleList) {
        //TODO send a redRequest to coordinator in the case of Quorum
        articleList.put(0,"Dummy article");
        Integer articleID = Integer.parseInt(ID);
        System.out.println("Article ID is " + articleID);
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
            objectOutputStream.writeObject(flag);
            objectOutputStream.writeObject(dependencyList);
            objectOutputStream.writeObject(sb.toString());
            System.out.println("Sent to Coordinator");
            receiveMessagefromCoordinator(coordinator, message);

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
            receiveMessagefromCoordinator(coordinator, message);

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

    private static void sendArticlesToClient(Socket client, SocketConnection coordinator, Consistency type, HashMap<Integer, String> articleList, HashMap<Integer, ArrayList<Integer>> dependencyList, String[] message, ObjectInputStream clois, ObjectOutputStream cloos) {
        //Send the whole object to the client
        ObjectOutputStream output = null;
        try {
            /*
                The following dummy entries has to be deleted before submitting
             */
//            articleList.put(0,"Dummy article");
//            ArrayList<Integer> dummyList = new ArrayList<>();
//            dummyList.add(0);
//            dependencyList.put(1, dummyList);
            if(type.equals(Consistency.SEQUENTIAL)) {
              //  output = new ObjectOutputStream(client.getOutputStream());
                output = cloos;
                output.writeObject(Server.articleList);
                output.writeObject(Server.dependencyList);
                output.reset();
                System.out.println("Sent to Client: Article: " + Server.articleList + " Dependency: " + dependencyList);
            }
            else if(type.equals(Consistency.QUORUM)){
                ServerHelper.sendReadToCoordinator(coordinator,message);
            }
        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }
    }

    public static void sendMessageToClient (Socket socket, String message){
        //Send the string message to the client
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(message);
            System.out.println("Sent the article to Client");

        } catch (IOException e) {
            System.out.println("Can't send message back to the client");
        }

    }


    public static void receiveMessagefromCoordinator (SocketConnection socket, String[] message){

        //TODO read DS from coordinator

        /*
            process message to do the needful
         */

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = socket.getOis();
            String readMesage = (String) objectInputStream.readObject();
            String dependency = (String) objectInputStream.readObject();
            System.out.println("dpen list " + dependency);
            HashMap<Integer, ArrayList<Integer>> dependencyList = convertToMap(dependency);
            System.out.println("Message from Coordiantor" + readMesage);
            String [] rec = readMesage.split(" ");
            String msg = "";
            for(int i = 1; i < rec.length; i++) {
                msg += rec[i];
            }
            Server.articleList.put(Integer.parseInt(rec[0]), msg);
            System.out.println("Message from coordinator::: "+dependencyList);
            if(dependencyList!=null)
                Server.dependencyList.putAll(dependencyList);
            System.out.println("Updated Dependency List "+Server.dependencyList);
        } catch (IOException e) {
            System.out.println("Can't read from coordinator");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't convert the message received from coordinator");
        }


        //TODO is it not getting updated. LEt us do a end to end testing tonight.
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