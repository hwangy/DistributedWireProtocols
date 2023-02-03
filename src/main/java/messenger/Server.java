package messenger;
import messenger.objects.*;

import java.io.*;
import java.net.*;  
import java.util.*;

public class Server {

    private Map<String, ArrayList<Message>> sentMessages;
    private Map<String, ArrayList<Message>> queuedMessages;
    private Set<String> loggedInUsers;

    public static void main(String[] args){
        try{
            System.out.println("Starting server...");
            ServerSocket ss=new ServerSocket(6666);
            System.out.println("Waiting for connection...");
            Socket s=ss.accept();//establishes connection

            String options = "Pick a method:\n" +
            "1. Create an account. You must supply a unique user name.\n" +
            "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
            "3. Send a message to a recipient.\n" +
            "4. Deliver undelivered messages to a particular user.\n" +
            "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)";

            while(true) {
                System.out.println(options);
                break;
            }

            DataInputStream dis=new DataInputStream(s.getInputStream());
            String  str=(String)dis.readUTF();
            System.out.println("message= "+str);
            ss.close();

        }catch(Exception e) {
            System.out.println(e);
        }
    }

}  