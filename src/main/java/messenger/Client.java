package messenger;
import messenger.api.APIException;
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

    // The username associated with the client; if this is not
    // set, then only the `createUser` method can be called.
    private static String username = null;

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 6666);
            connection = new Connection(socket);
            ClientCore client = new ClientCore();

            // Ask user for the server's IP address

            //Socket s=new Socket("10.250.94.79",6666);

            String options = "Pick an option:\n" +
                    "0. Exit (log out).\n" +
                    "1. Create an account. You must supply a unique user name.\n" +
                    "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                    "3. Send a message to a recipient.\n" +
                    "4. Deliver undelivered messages to a particular user.\n" +
                    "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)\n" +
                    "6. Log in to an existing account.\n";

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
                
                try {
                    choice = Integer.parseInt(inputReader.nextLine());
                } catch (NumberFormatException e) {
                    // Invalid choice selected.
                    Logging.logService("Invalid choice selected, please try again.");
                    continue;
                }
               
                if (choice < 0|| choice > 6) {
                    // throw an exception?
                    System.out.println("Please enter a number between 1 and 6.");
                    continue;
                } //else if (choice == 0) {
                    //inputReader.close();
                   // break;
                //}

                API method;
                try {
                    method = API.fromInt(choice);
                } catch (APIException ex) {
                    // Invalid choice selected.
                    Logging.logService("Invalid choice selected, please try again.");
                    continue;
                }

                // The user should only be allowed to select a method
                // besides `CREATE_ACCOUNT` or `LOGIN` if the username is set.
                if (method != API.CREATE_ACCOUNT && method != API.LOGIN && username == null) {
                    Logging.logService("Please first create a username or log in, by selecting option "
                            + API.CREATE_ACCOUNT.getIdentifier() + " or " + API.LOGIN.getIdentifier());
                    continue;
                }

                System.out.println("You have chosen option: " + choice);
                if (method == API.LOGOUT) {
                    client.getUsername();
                    System.out.println("Logging out of the account associated to the username: " + username);

                    System.out.println("Attempting to log out...");
                    LogoutRequest request = new LogoutRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // If acceptable response (change to account for this)
                    client.setUsername(null);
                    inputReader.close();
                    break;

                } else if (method == API.CREATE_ACCOUNT) {
                    System.out.println("Pick your username.");
                    username = inputReader.nextLine();
                    System.out.println("Username: " + username);

                    //Logging.logDebug("Testing create user");
                    System.out.println("Attempting to create a new account...");
                    CreateAccountRequest request = new CreateAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // Have message for if username already attached to account.
                    // If acceptable response (change to account for this)
                    client.setUsername(username);

                } else if (method == API.GET_ACCOUNTS){
                    String text_wildcard = "";
                    System.out.println("Optionally, specificy a text wildcard. Else press enter.");
                    text_wildcard = inputReader.nextLine();

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
                    recipient = inputReader.nextLine();
                    // Maybe check with server if it's a real recipient first?
                    System.out.println("Recipient: " + recipient);

                    System.out.println("Specify your message.");
                    message = inputReader.nextLine();
                    System.out.println("Message: " + message);
                    // Is it ok to only handle 1-line messages?
                    username = client.getUsername();

                    SendMessageRequest request = new SendMessageRequest(username, recipient, message);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                } else if (method == API.GET_UNDELIVERED_MESSAGES){
                    String username = "";
                    System.out.println("Specify the username of the account to deliver undelivered messages to.");
                    username = inputReader.nextLine();
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
                    username = client.getUsername();
                    System.out.println("Deleting the account associated to the username: " + username);
                    //username = inputReader.nextLine();
                    //System.out.println("Username: " + username);
                    //Logging.logDebug("Test delete account");
                    DeleteAccountRequest request = new DeleteAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // If successful (change to account for this)
                    client.setUsername(null);
                } else if (method == API.LOGIN) {
                    System.out.println("Select the username.");
                    username = inputReader.nextLine();
                    System.out.println("Username: " + username);

                    System.out.println("Attempting to log in...");
                    LoginRequest request = new LoginRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // If acceptable response (change to account for this)
                    client.setUsername(username);

                }

            }
            connection.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }  
}