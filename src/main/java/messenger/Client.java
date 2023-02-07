package messenger;
import messenger.objects.*;
import messenger.objects.helper.API;
import messenger.objects.request.*;

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


        Scanner input_reader = new Scanner(System.in); 

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

            System.out.println(options);
            /*choice = input_reader.nextInt();
            if (choice < 1|| choice > 6) {
                // throw an exception
            }*/

            choice = 5;
            API method = API.fromInt(choice);
            if (method == API.CREATE_USER) {
                /*
                Ask user for argument
                 */
                String username = "test";
                System.out.println("Test create user");
                CreateUserRequest request = new CreateUserRequest(username);
                client.sendRequest(request.genGenericRequest());
                // Wait for response
                //TODO: I guess we'll need two threads, one for sending and another for receiving
            } else if (method == API.GET_ACCOUNTS){
                String text_wildcard = "test";
                System.out.println("Test get accounts");
                GetAccountsRequest request = new GetAccountsRequest(text_wildcard);
                client.sendRequest(request.genGenericRequest());
                // Wait for response and handle response
            } else if (method == API.SEND_MESSAGE) {
                String recipient = "test";
                System.out.println("Test send message");
                SendMessageRequest request = new SendMessageRequest(recipient);
                client.sendRequest(request.genGenericRequest());
                // Wait for response and handle response
            } else if (method == API.GET_UNDELIVERED_MESSAGES){
                String username = "test";
                System.out.println("Test get undelivered messages");
                GetUndeliveredMessagesRequest request = new GetUndeliveredMessagesRequest(username);
                client.sendRequest(request.genGenericRequest());
                // Wait for response and handle response
            } else if (method ==API.DELETE_ACCOUNT) {
                // Question: If the user is logged in, do we want to ask for username?
                String username = "test";
                System.out.println("Test delete account");
                DeleteAccountRequest request = new DeleteAccountRequest(username);
                client.sendRequest(request.genGenericRequest());
            }

            break;
        }
        client.closeConnections();
        socket.close();
    }catch(Exception e){System.out.println(e);}
    }  

}  