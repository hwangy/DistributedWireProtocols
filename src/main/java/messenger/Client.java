package messenger;
import messenger.objects.*;

import java.io.*;
import java.net.*;  
import java.util.*;

public class Client {  

    private Map<String, ArrayList<Message>> receivedMessages;

    public static void main(String[] args) {  
    try{
        // Ask user for the server's IP address

        //Socket s=new Socket("10.250.94.79",6666);
        Socket s=new Socket("localhost",6666);
        DataOutputStream dout=new DataOutputStream(s.getOutputStream());
        dout.writeUTF("Hello Server");

        while (true) {
            /*
            1. Ask for choice of method
            2. Get arguments for method
            IF
                3. [Method]Request req = gen[Method]Request(String arguments...)
                4. String rawRequest = encodeRequest(req.getGenericRequest());
                5. sendToServer(rawRequest());
                6. String response = getFromServer();
                7. [Method]Response response = [Method]Response.parseResponse(Response.fromString(response));
                8. Take some action based on the server's response
                    (e.g. adding messages to hashmap, telling user there was a failure)
             */
            break;
        }
        dout.flush();  
        dout.close();  
        s.close();
    }catch(Exception e){System.out.println(e);}
    }  

}  