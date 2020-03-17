import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class CoordinatorHelper {

    public static ArrayList<String> getServerIPAndPort(){
        ArrayList<String> listOfServers = new ArrayList<>();
        // Decide how to store the IP and Port information of all the servers. Preferably in a list.

        return listOfServers;
    }

    public static String receiveMessageFromServer(Socket socket, HashMap<String, String> serverMessageQueue, int ID) {
        String result = null;
        int latestID = 0;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String message = in.readUTF();
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

        return result;
    }

    public static int getLatestID(Socket socket, String message, int ID){
        return ID += ID;
    }

    public static void broadcastMessageToServers(Socket socket, String message) throws IOException {
        ArrayList<String> listOfServers = getServerIPAndPort();
        for (String server : listOfServers) {
            String[] IPAndPort = server.split(":");
            String IP = IPAndPort[0];
            String port = IPAndPort[1];
            //I think a new socket for each of the servers will be created for them to receive the message. Not sure though?
            //Also, the dependency list should be same in all the servers, so even that should be passed to the coordinator and then passed on
            // to other servers, no?
            Socket serverSocket = new Socket(IP, Integer.parseInt(port));
            DataOutputStream output = new DataOutputStream(serverSocket.getOutputStream());
            output.writeUTF(message);
            System.out.println("Sent to Socket");
            output.close();
            serverSocket.close();
        }
    }
}
