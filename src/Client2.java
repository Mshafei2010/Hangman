import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client2 {
    private static String message = "Hello World";
    private static Scanner input = new Scanner(System.in);

    static void login(ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while(true){
            System.out.println("Enter Username: ");
            String username= input.nextLine();
            oos.writeObject(username);
            System.out.println("Enter password: ");
            String password= input.nextLine();
            oos.writeObject(password);
            String answer = (String) ois.readObject();
            System.out.println(answer);
            if (answer.equals("Login Successful") ){
                break;
            }
            oos.flush();
        }
    }

    static void signUp(ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while(true){
            System.out.println("Enter Name: ");
            String name= input.nextLine();
            oos.writeObject(name);
            System.out.println("Enter Username: ");
            String username= input.nextLine();
            oos.writeObject(username);
            System.out.println("Enter password: ");
            String password= input.nextLine();
            oos.writeObject(password);
            String answer = (String) ois.readObject();
            System.out.println(answer);
            if (answer.equals("Username added successfully") ){
                break;
            }
            oos.flush();
        }
    }

    static void setupGame(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //The users’ login credentials
        //username
        System.out.println(ois.readObject());
        //password
        System.out.println(ois.readObject());
        //Name
        System.out.println(ois.readObject());

        //Score history for each user
        System.out.println(ois.readObject());


        //The game configuration (the number of incorrect guesses (attempts) needed to lose the game,
        // the maximum and minimum number of players per team or game room
        //"Welcome to Hangman"
        System.out.println(ois.readObject());
        //Attempts
        System.out.println(ois.readObject());
        //min players
        System.out.println(ois.readObject());
        //max players
        System.out.println(ois.readObject());

    }
    static void Game (ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while(true)
        {
            //Number of Attempts
            System.out.println(ois.readObject());
            //Word in dashes
            System.out.println(ois.readObject());
            //Guess
            System.out.print(ois.readObject());
            String guess = input.nextLine();
            oos.writeObject(guess);
            //Correct or Incorrect Guess or won game
            String reply= (String) ois.readObject();
            System.out.println(reply);
            if (reply.equals("You Won") || reply.equals("You Lost"))
                break;
        }
    }



    //connect to Socket
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), 5000);
            System.out.println("Connected to server");
            System.out.println("Sending request to Socket Server");
            //Read from Server
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //write to Server
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("1-Login\n2-Sign Up");
            String choice = input.nextLine();
            System.out.println(choice);
            oos.writeObject(choice);
            if (choice.equals("1"))
            {
                login(ois,oos);
            }
            else if (choice.equals("2"))
                signUp(ois,oos);
            setupGame(ois);
            Game(ois,oos);
            setupGame(ois);
            oos.close();
        } catch (UnknownHostException e) {
            System.out.println("Host not found");
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
