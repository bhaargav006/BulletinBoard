import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    //TODO: How do I connect to a particular server
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("----******Welcome to the Bulletin Board******-----");
        System.out.println("Here are the list of actions that you can perform!");
        System.out.println("");
        String message = "";
        //TODO: Change the data sent based on the input from the user
        InetAddress host = InetAddress.getLocalHost();
        try {
            SocketConnection socketConnection = new SocketConnection(8000);
            Boolean exit = true;
            while(exit){
                System.out.println("[P] Post \n[R] Read \n[C] Choose \n[Rep] Reply \n");
                Scanner in = new Scanner(System.in);
                String chosenAction = in.nextLine();
                switch (chosenAction){
                    case "P":
                        message = "Post blah blah";
                        System.out.print("Enter Article:\n");
                        String article = in.nextLine();
                        message = "Post "+article;
                        break;
                    case "R":
                        message = "Read";
                        break;
                    case "C":
                        message = "Choose 0";
                        break;
                    case "Rep":
                        message = "Reply dnfdfn";
                        break;
                    case "Exit":
                       // socket.close();
                        exit = false;
                        break;
                    default:
                        System.out.println("Not a valid input!");
                }
                System.out.println(message);
                ClientHelper.processMessage(socketConnection,message);
            }

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the server");
        }

    }
}
