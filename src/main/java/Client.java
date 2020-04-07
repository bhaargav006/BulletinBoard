import java.io.IOException;
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

                    case "C": {
                        System.out.print("Enter Article Id:\n");
                        String id = in.nextLine();
                        message = "Choose " + id;
                        break;
                    }
                    case "Rep":
                        System.out.println("Enter Article Id");
                        String id = in.nextLine();
                        System.out.println("Enter Article");
                        String msg = in.nextLine();
                        message = "Reply " +id +" " +msg;
                        break;
                    case "Exit":
                        socketConnection.close();
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
