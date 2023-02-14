package messenger;
import messenger.api.APIException;
import messenger.network.Connection;
import messenger.objects.*;
import messenger.api.API;
import messenger.objects.request.*;
import messenger.objects.response.Response;
import messenger.objects.response.StatusMessageResponse;
import messenger.util.Logging;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {

    private Map<String, ArrayList<Message>> receivedMessages;

    private static Connection connection;
    private static Connection messageConnection;
    private static Socket messageSocket;

    // The username associated with the client; if this is not
    // set, then only the `createUser` method can be called.
    private static String username = null;

    public static Connection launchMessageReceiver() throws IOException {
        Logging.logDebug("Looking for connection...");
        messageSocket = new Socket("localhost", 7777);
        Connection connection = new Connection(messageSocket);
        MessageReceiver receiver = new MessageReceiver(connection);
        new Thread(receiver).start();

        return connection;
    }

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
                } 
                
                API method;
                try {
                    method = API.fromInt(choice);
                } catch (APIException ex) {
                    // Invalid choice selected.
                    Logging.logService("Invalid choice selected, please try again.");
                    continue;
                }

                // The user should only be allowed to select a method
                // `CREATE_ACCOUNT` or `LOGIN` if the username is not set.
                if (method != API.CREATE_ACCOUNT && method != API.LOGIN && username == null) {
                    Logging.logService("Please first create a username or log in, by selecting option "
                            + API.CREATE_ACCOUNT.getIdentifier() + " or " + API.LOGIN.getIdentifier());
                    continue;
                }

                Logging.logService("You have chosen option: " + choice);
                if (method == API.LOGOUT) {
                    client.getUsername();
                    Logging.logService("Logging out of the account associated to the username: " + username);

                    Logging.logService("Attempting to log out...");
                    LogoutRequest request = new LogoutRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    StatusMessageResponse statusResponses = new StatusMessageResponse(responses);
                    client.logoutAPI(statusResponses);
                    inputReader.close();
                    break;

                } else if (method == API.CREATE_ACCOUNT) {
                    Logging.logService("Pick your username.");
                    username = inputReader.nextLine();
                    Logging.logService("Username: " + username);

                    //Logging.logDebug("Testing create user");
                    Logging.logService("Attempting to create a new account...");
                    CreateAccountRequest request = new CreateAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    // Have message for if username already attached to account.

                    LoginRequest login_request = new LoginRequest(username);
                    StatusMessageResponse statusResponses = new StatusMessageResponse(responses);
                    client.loginAPI(login_request, statusResponses);

                    Logging.logDebug("Launching message receiver.");
                    launchMessageReceiver();
                } else if (method == API.GET_ACCOUNTS){
                    String text_wildcard = "";
                    Logging.logService("Optionally, specificy a text wildcard. Else press enter.");
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
                    Logging.logService("Pick your recipient.");
                    recipient = inputReader.nextLine();
                    // Maybe check with server if it's a real recipient first?
                    Logging.logService("Recipient: " + recipient);

                    Logging.logService("Specify your message.");
                    message = inputReader.nextLine();
                    Logging.logService("Message: " + message);
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
                    //System.out.println("Specify the username of the account to deliver undelivered messages to.");
                    //username = inputReader.nextLine();
                    //System.out.println("Username: " + username);
                    username = client.getUsername();
                    Logging.logService("Delivering undelivered messages to: " + username);
                    
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

                    StatusMessageResponse statusResponses = new StatusMessageResponse(responses);
                    client.logoutAPI(statusResponses);
                } else if (method == API.LOGIN) {
                    Logging.logService("Select the username.");
                    username = inputReader.nextLine();
                    Logging.logService("Username: " + username);

                    Logging.logService("Attempting to log in...");
                    LoginRequest request = new LoginRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }

                    StatusMessageResponse statusResponses = new StatusMessageResponse(responses);
                    client.loginAPI(request, statusResponses);
                }

            }
            connection.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public static class MessageReceiver implements Runnable {
        private final Connection connection;
        public MessageReceiver(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                Logging.logDebug("Waiting for messages...");
                while (true) {
                    Message message = Message.genMessage(connection);
                    Logging.logInfo(message.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}