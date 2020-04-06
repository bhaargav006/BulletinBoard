import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Coordinator {


    static volatile HashMap<String, String> serverMessageQueue;
    static volatile int ID;
    static volatile ArrayList<SocketConnection> serverSockets;
    Consistency type;

    public Coordinator(int port) {
        serverMessageQueue = new HashMap();
        ID = 0;
        type = Consistency.ERROR;
        serverSockets = new ArrayList();

        while (type.equals(Consistency.ERROR)) {
            System.out.println("Hello Coordinator, Choose a type of consistency:");
            System.out.println("1. [Seq] Sequential Consistency \n2. [Quo] Quorum Consistency \n" +
                    "3. [RYW] Read your write Consistency\n4.[Exit] Exit");
            Scanner in = new Scanner(System.in);
            String consistency = in.nextLine();
            type = CoordinatorHelper.getConsistencyType(consistency);
            if (type.equals(Consistency.EXIT))
                System.exit(0);
        }

        System.out.println("Starting Coordinator and implementing " + type.toString() + " Consistency");
        ServerSocket coordinator;
        try {
            coordinator = new ServerSocket(port);
            while(true){
                Socket server = null;
                try {
                    server = coordinator.accept();
//                    ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());
//                    ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                    SocketConnection sc = new SocketConnection(server);
                    Thread serverResponder = new ServerResponder(sc,type);
                    serverResponder.start();


                } catch (IOException e) {
                    System.out.println("Error in the Coordinator sockets while accepting server");
                }
            }
        } catch (IOException e) {
            System.out.println("Couldn't create connection with server");
        }

    }

    public static void main(String[] args) {
        Coordinator coordinator = new Coordinator(8001);
    }
}


