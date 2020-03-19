import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        System.out.println("----******Welcome to the Bulletin Board******-----");
        System.out.println("Select any number to read more about the article");
        System.out.println("List of Articles:");
        List<Tuple<Integer, String>> listOfArticlesHead = new ArrayList<Tuple<Integer, String>>();
        listOfArticlesHead.add(new Tuple<Integer, String>(1, "This is head of article 1"));
        listOfArticlesHead.add(new Tuple<Integer, String>(2, "This is head of article 2"));
        listOfArticlesHead.add(new Tuple<Integer, String>(3, "This is head of article 3"));
        listOfArticlesHead.add(new Tuple<Integer, String>(4, "This is head of article 4"));
        //TODO: Change the data sent based on the input from the user
        InetAddress host = InetAddress.getLocalHost();
        try {
            Socket socket = new Socket(host, 8000);
            String message = "Post blah blah";
            ClientHelper.processMessage(socket,message);
            socket.close();

        } catch (IOException e) {
            System.out.println("Error occurred while communicating with the server");

            System.out.println("Enter your choice:");
            System.out.println("[A]. Read Articles" + "\n" + "[B]. Post Article" + "\n" + "[C]. Reply to a Article" + "\n" + "[D]. Next Set of Articles" + "[E]. Exit");
            Scanner in = new Scanner(System.in);
            while (in.hasNext()) {
                String s = in.nextLine();
                if (s.equals("E")) break;
                System.out.println("You selected " + s);

            }
        }
    }
    //TODO: change to different class
    public static class Tuple<T,S>{
        T t;
        S s;
        public Tuple(T t, S s) {
            this.t = t;
            this.s = s;
        }

    }
}
