import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {

    static ArrayList<String> users =new ArrayList<String>();
    static int readcounter=0;

    static void readusers(ArrayList users) throws IOException {
        if (users != null)
            users.clear();
        File file = new File("users.txt");
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            users.add(line);
        }
        bufferedReader.close();
        reader.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //make Socket for server
        ServerSocket serverSocket = new ServerSocket(5000);
        while (true){
            System.out.println("Waiting for client request");
            Socket socket = serverSocket.accept();
            System.out.println("Connected to client");
            //write to client
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //read from client
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            if (readcounter==0)
            {
                readusers(users);
                readcounter++;
            }
            Thread myThread = new hangmanThread(socket,ois,oos,users);

            myThread.start();
        }

//        ois.close();
//        socket.close();
//        System.out.println("Shutting down Socket server!!");
//        //close the ServerSocket object
//        serverSocket.close();


    }
}