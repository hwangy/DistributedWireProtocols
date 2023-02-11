package messenger;
import messenger.objects.*;
import messenger.objects.helper.API;
import messenger.objects.request.CreateAccountRequest;

import java.io.*;
import java.net.*;  
import java.util.*;

public class Client {

    private Map<String, ArrayList<Message>> receivedMessages;

    public static void main(String[] args) {
    try{
        Socket socket = new Socket("localhost", 6666);
        ClientCore client = new ClientCore(
                new DataOutputStream(socket.getOutputStream()),
                new DataInputStream(socket.getInputStream()));

        // Ask user for the server's IP address

        //Socket s=new Socket("10.250.94.79",6666);

        String options = "Pick a method:\n" +
                "1. Create an account. You must supply a unique user name.\n" +
                "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                "3. Send a message to a recipient.\n" +
                "4. Deliver undelivered messages to a particular user.\n" +
                "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)";


        int choice = -1;
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
            choice = 1;
            API method = API.fromInt(choice);
            if (method == API.CREATE_ACCOUNT) {
                /*
                Ask user for argument
                 */
                String username = "test";
                System.out.println("TEST");
                CreateAccountRequest request = new CreateAccountRequest(username);
                client.sendRequest(request.genGenericRequest());
                // Wait for response
                //TODO: I guess we'll need two threads, one for sending and another for receiving
            }
            break;
        }
        client.closeConnections();
        socket.close();
    }catch(Exception e){System.out.println(e);}
    }  

}  