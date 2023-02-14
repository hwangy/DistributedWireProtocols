package messenger;
import messenger.network.Connection;
import messenger.objects.*;
import messenger.api.API;
import messenger.objects.request.*;
import messenger.objects.response.Response;
import messenger.util.Logging;

import java.net.*;
import java.util.*;

public class Client {

    private Map<String, ArrayList<Message>> receivedMessages;

    private static Connection connection;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6666);
            connection = new Connection(socket);
            ClientCore client = new ClientCore();

            // Ask user for the server's IP address

            //Socket s=new Socket("10.250.94.79",6666);

            String options = "Pick an option:\n" +
                    "1. Create an account. You must supply a unique user name.\n" +
                    "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                    "3. Send a message to a recipient.\n" +
                    "4. Deliver undelivered messages to a particular user.\n" +
                    "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)\n" +
                    "6. Exit (log out)";

            Scanner inputReader = new Scanner(System.in);

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

                //Logging.logDebug("In scanner component");
                //String line;
                //while(inputReader.hasNextInt()) {
                    //choice = inputReader.nextInt();
                    //Logging.logDebug("Read \"" + line + "\"");
                //}
                
                choice = inputReader.nextInt();
                if (choice < 1|| choice > 6) {
                    // throw an exception
                } else if (choice == 6) {
                    inputReader.close();
                    break;
                }

                System.out.println("You have chosen option: " + choice);

                API method = API.fromInt(choice);
                if (method == API.CREATE_ACCOUNT) {
                    String username = "";
                    System.out.println("Pick your username.");
                    username = inputReader.next();
                    System.out.println("Username: " + username);

                    //Logging.logDebug("Testing create user");
                    System.out.println("Creating new account...");
                    CreateAccountRequest request = new CreateAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // Have message for if username already attached to account.

                    // Get response
                } else if (method == API.GET_ACCOUNTS){
                    String text_wildcard = "";
                    System.out.println("Optionally, specificy a text wildcard. Else press enter.");
                    text_wildcard = inputReader.next();
                    if(text_wildcard.equals("")){
                        System.out.println("Proceeding with no wildcard.");
                    } else{
                        System.out.println("Text wildcard: " + text_wildcard);
                        // need to fix this, next doesn't like enter key but nextLine doesn't work well with client/server.
                    } 
                    
                    // Make sure to handle case of text wildcard empty (search everything) or nonempty.
                    // And are there text wildcards we would disallow or that could cause issues?
                    //Logging.logDebug("Test get accounts");
                    GetAccountsRequest request = new GetAccountsRequest(text_wildcard);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                } else if (method == API.SEND_MESSAGE) {
                    // Do we need to handle case of empty message?
                    String recipient = "";
                    String message = "";
                    System.out.println("Pick your recipient.");
                    recipient = inputReader.next();
                    // Maybe check with server if it's a real recipient first?
                    System.out.println("Recipient: " + recipient);

                    System.out.println("Specify your message.");
                    message = inputReader.next();
                    System.out.println("Message: " + message);
                    // Is it ok to only handle 1-line messages?

                    //Logging.logDebug("Test send message");
                    SendMessageRequest request = new SendMessageRequest(recipient, message);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                } else if (method == API.GET_UNDELIVERED_MESSAGES){
                    String username = "";
                    System.out.println("Specify the username of the account to deliver undelivered messages to.");
                    username = inputReader.next();
                    System.out.println("Username: " + username);
                    
                    //Logging.logDebug("Test get undelivered messages");
                    GetUndeliveredMessagesRequest request = new GetUndeliveredMessagesRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                } else if (method ==API.DELETE_ACCOUNT) {
                    // Question: If the user is logged in, do we want to ask for username?
                    String username = "";
                    System.out.println("Specify the username of the account to delete.");
                    username = inputReader.next();
                    System.out.println("Username: " + username);
                    //Logging.logDebug("Test delete account");
                    DeleteAccountRequest request = new DeleteAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                } 

            }
            connection.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }  
}