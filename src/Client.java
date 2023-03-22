import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static String message = "Hello World";
    private static Scanner input = new Scanner(System.in);

    static Boolean login(ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while(true){
            System.out.println("Enter Username: ");
            String username= input.nextLine();
            oos.writeObject(username);
            if (username.equals("-")){
                break;
            }
            System.out.println("Enter password: ");
            String password= input.nextLine();
            oos.writeObject(password);
            if (password.equals("-")){
                break;
            }
            String answer = (String) ois.readObject();
            System.out.println(answer);
            if (answer.equals("Login Successful") ){
                return true;
            }
            oos.flush();
        }
        return false;
    }

    static Boolean signUp(ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while(true){
            System.out.println("Enter Name: ");
            String name= input.nextLine();
            oos.writeObject(name);
            if (name.equals("-")){
                break;
            }
            System.out.println("Enter Username: ");
            String username= input.nextLine();
            oos.writeObject(username);
            if (username.equals("-")){
                break;
            }
            System.out.println("Enter password: ");
            String password= input.nextLine();
            oos.writeObject(password);
            if (password.equals("-")){
                break;
            }
            String answer = (String) ois.readObject();
            System.out.println(answer);
            if (answer.equals("Username added successfully") ){
                return true;
            }
            oos.flush();
        }
        return false;
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
    static Boolean Game (ObjectInputStream ois,ObjectOutputStream oos) throws IOException, ClassNotFoundException {
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
            if (guess.equals("-"))
                return true;
            //Correct or Incorrect Guess or won game
            String reply= (String) ois.readObject();
            System.out.println(reply);
            if (reply.equals("You Won") || reply.equals("You Lost"))
                return false;
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
            oos.writeObject(choice);
            Boolean logged=false;
            if (choice.equals("1"))
            {
                logged=login(ois,oos);
            }
            else if (choice.equals("2"))
                logged=signUp(ois,oos);
            if(logged==true)
            {
                setupGame(ois);
                //Choose Mode
                System.out.println(ois.readObject());
                String mode = input.nextLine();
                oos.writeObject(mode);
                Boolean end=true;
                while (true)
                {
                    if(mode.equals("1")) {
                        if (!end)
                            setupGame(ois);
                        end = Game(ois, oos);
                        if (end)
                            break;
                    }
                    else if (mode.equals("2")) {
                        System.out.println(ois.readObject());
                        break;
                    }
                    else{
                        System.out.println(ois.readObject());
                        System.out.println(ois.readObject());
                        mode = input.nextLine();
                        oos.writeObject(mode);
                        oos.flush();
                    }
                }
                setupGame(ois);
            }
            oos.close();
            ois.close();
            System.out.println("Shutting down Socket client!!");
            socket.close();
        } catch (UnknownHostException e) {
            System.out.println("Host not found");
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
