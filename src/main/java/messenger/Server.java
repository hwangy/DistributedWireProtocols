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
            DataInputStream dis=new DataInputStream(s.getInputStream());
            String  str=(String)dis.readUTF();
            System.out.println("message= "+str);
            ss.close();

        }catch(Exception e) {
            System.out.println(e);
        }
    }

}  