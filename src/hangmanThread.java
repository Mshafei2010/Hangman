import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Random;

public class hangmanThread extends Thread{
    private static Scanner input = new Scanner(System.in);
    ObjectInputStream ois;
    ObjectOutputStream oos ;
    Socket socket;
    static ArrayList<String> users =new ArrayList<String>();
    static int [] scores =new int [5];

    static int incorrectGuesses = 6;
    static int maxPlayers = 3;
    static int minPlayers = 2;

    static ArrayList<String> words = new ArrayList<String>() ;

    static String username = "";

    public hangmanThread(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, ArrayList<String> users){
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        this.users = users;
    }

    Boolean login(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while (true) {
            Boolean success = false;
            String username = (String) ois.readObject();
            System.out.println("Username Received: " + username);
            if (username.equals("-")){
                return false;
            }
            String password = (String) ois.readObject();
            if (password.equals("-")){
                return false;
            }
            for (int i = 0; i < users.size(); i+=3) {
                if (username.equals(users.get(i)) && password.equals(users.get(i+1))) {
                    System.out.println("Login Successful");
                    oos.writeObject("Login Successful");
                    success = true;
                    this.username = username;
                    break;
                }
            }
            if (success == true) {
                break;
            } else {
                System.out.println("Login Failed");
                Boolean flag=true;
                for (int i = 0; i < users.size(); i+=3) {
                    if (username.equals(users.get(i)))
                    {
                        oos.writeObject("unauthorized");
                        flag=false;
                        break;
                    }
                }
                if (flag)
                    oos.writeObject("not found");
            }
            oos.flush();
        }
        return true;
    }

    Boolean SignUp(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        while (true) {
            Boolean found = false;
            String name = (String) ois.readObject();
            if (name.equals("-")){
                return false;
            }
            System.out.println("name Received: " + name);
            String username = (String) ois.readObject();
            if (username.equals("-")){
                return false;
            }
            System.out.println("Username Received: " + username);
            String password = (String) ois.readObject();
            if (password.equals("-")){
                return false;
            }
            System.out.println("password Received: " + password);
            for (int i = 0; i < users.size(); i+=3) {
                if (username.equals(users.get(i)) ){
                    System.out.println("Username already exists");
                    oos.writeObject("Username already exists");
                    found = true;
                    break;
                }
            }
            if (found == false) {
                writeusers(users , username , password , name);
                this.username = username;
                //create score file for user
                createscorefile();
                oos.writeObject("Username added successfully");
                break;
            }
            oos.flush();
        }
        return true;
    }

    void setupGame(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        //The users’ login credentials
        oos.writeObject("-------------------------------------------\nUsername: " + this.username);
        for (int i = 0; i < users.size(); i+=3) {
            if (username.equals(users.get(i)) ){
                oos.writeObject("Password: " + users.get(i+1));
                oos.writeObject("Name: " + users.get(i+2));
                break;
            }
        }

        //Score history for each user
        oos.writeObject("Single Score:\nWin: " + scores[0] + "\nLoss: " + scores[1] + "\nMultiplayer Score:\nWin: " + scores[2]+"\nLose: " + scores[3] +"\nDraw: " + scores[4]);
        oos.flush();
        //The game configuration (the number of incorrect guesses (attempts) needed to lose the game,
        // the maximum and minimum number of players per team or game room
        incorrectGuesses=6;
        oos.writeObject("-------------------------------------------\nWelcome to Hangman " + username);
        oos.writeObject("Number of incorrect guesses: " + incorrectGuesses);
        oos.writeObject("Minimum number of players: " + minPlayers);
        oos.writeObject("Maximum number of players: " + maxPlayers + "\n-------------------------------------------" );
        oos.flush();

        //lookup file containing the phrases
        File file = new File("words.txt");
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            words.add(line);
        }
        bufferedReader.close();
        reader.close();
    }

    static Boolean Game (ObjectInputStream ois, ObjectOutputStream oos, String playingword, int wordlength) throws IOException, ClassNotFoundException {
        int Attempts=6;
        String dashes = "";
        for (int i = 0; i < wordlength; i++) {
            dashes += "_";
        }
        while(Attempts>0)
        {
            oos.writeObject("Number of Attempts left: "+ Attempts );
            oos.writeObject("Word: "+dashes +"\nif you want to quit enter -" );
            oos.writeObject("Your Guess: ");
            oos.flush();
            String guess= (String) ois.readObject();
            guess = guess.toLowerCase(Locale.ROOT);
            try {
                guess=guess.substring(0,1);
                if(guess.equals("-"))
                    return true;
                if (playingword.contains(guess)){
                    for (int i = 0; i < wordlength; i++) {
                        if (playingword.charAt(i)==guess.charAt(0))
                        {
                            dashes=dashes.substring(0,i)+guess+dashes.substring(i+1);
                        }
                    }
                    if (dashes.equals(playingword))
                    {
                        oos.writeObject("You Won");
                        updatescore(0);
                        return false;
                    }
                    else {
                        oos.writeObject("Correct Guess");
                    }
                }
                else
                {
                    Attempts--;
                    if(Attempts==0)
                    {
                        oos.writeObject("You Lost");
                        updatescore(1);
                        return false;
                    }
                    else
                        oos.writeObject("Incorrect Guess");
                }
            }
            catch (Exception e)
            {
                oos.writeObject("Invalid Input" + e.getMessage());
            }
        }
        return false;
//        String username = (String) ois.readObject();
    }

    @Override
    public void run()
    {
        try {
            System.out.println("1-Login\n2-Sign Up");
            String choice = (String) ois.readObject();
            System.out.println(choice);
            Boolean loggedIn = false;
            if (choice.equals("1"))
            {
                System.out.println("Login");
                loggedIn=login(ois,oos);
            }
            else if (choice.equals("2"))
                loggedIn=SignUp(ois,oos);
            else if (choice.equals("-"))
                loggedIn=false;
            else
                oos.writeObject("Invalid Input");
            if (loggedIn)
            {
                readscore(scores);
                setupGame(ois,oos);
                while(true)
                {
                    oos.flush();
                    oos.writeObject("Choose Game Mode:\n1-Single Player\n2-Multiplayer");
                    String mode= (String) ois.readObject();
                    if (mode.equals("1"))
                    {
                        while (true)
                        {
                            String playingword = randomWord();
                            int wordlength = playingword.length();
                            Boolean end = Game(ois,oos,playingword,wordlength);
                            setupGame(ois,oos);
                            oos.flush();
                            if (end)
                                break;
                        }
                        break;
                    }
                    else if (mode.equals("2"))
                    {
                        oos.writeObject("Multiplayer");
                        setupGame(ois,oos);
                        break;
                    }
                    else
                    {
                        oos.writeObject("Invalid Input");
                    }
                }
                updateUserFile();
                updateScoreFile();
            }
            ois.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static String randomWord()
    {
        int wordindex = new Random().nextInt(words.size());
        String word = words.get(wordindex);
        System.out.println("Selected word: " + word);
        word = word.toLowerCase(Locale.ROOT);
        return word;
    }

    //Score
    static void createscorefile() throws IOException {
        File myObj = new File(username+".txt");
        if (myObj.createNewFile()) {
            FileWriter writer = new FileWriter(username+".txt");
            System.out.println("File created: " + myObj.getName());
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (int i = 0; i < 5; i++) {
                bufferedWriter.write("0");
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            writer.close();
        }
        else {
            System.out.println("File already exists.");
        }
    }
    static void readscore(int [] scores) throws IOException {
        File file = new File(username+".txt");
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        int counter=0;
        while ((line = bufferedReader.readLine()) != null) {
            scores[counter]= Integer.parseInt(line);
            counter++;
        }
        bufferedReader.close();
        reader.close();
    }
    static void updatescore(int index){
        //won = 0
        //lose = 1
        //won multi = 2
        //lose multi = 3
        //draw multi = 4
        scores[index]++;
    }
    static void updateScoreFile() throws IOException {
        File f = new File(username+".txt");
        if (f.isFile())
        {
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (int i = 0; i < scores.length; i++) {
                bufferedWriter.write(Integer.toString(scores[i]));
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            writer.close();
        }
    }
    //User
    static void writeusers(ArrayList users , String username , String password , String name ) throws IOException {
        users.add(username);
        users.add(password);
        users.add(name);
    }
    static void updateUserFile() throws IOException {
        FileWriter writer = new FileWriter("users.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        for (int i = 0; i < users.size(); i++) {
            bufferedWriter.write(users.get(i).toString());
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
        writer.close();
        System.out.println("File Updated");
    }
}
