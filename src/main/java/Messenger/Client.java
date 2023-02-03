package src.main.java.Messenger;

import java.io.*;
import java.net.*;  

public class Client {  

    private Map<String, Array<Message>> receivedMessages;

    public static void main(String[] args) {  
    try{      
        //Socket s=new Socket("10.250.94.79",6666);  
        Socket s=new Socket("localhost",6666);  
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
        dout.writeUTF("Hello Server");  
        dout.flush();  
        dout.close();  
        s.close();  
    }catch(Exception e){System.out.println(e);}  
    }  
    
}  