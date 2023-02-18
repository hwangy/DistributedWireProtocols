package messenger;

import messenger.api.APIException;
import messenger.network.Connection;
import messenger.objects.*;
import messenger.api.API;
import messenger.objects.request.*;
import messenger.objects.response.Response;
import messenger.objects.response.StatusMessageResponse;
import messenger.util.Constants;
import messenger.util.Logging;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {

    private static Connection connection;
    private static Connection messageConnection = null;
    private static Socket messageSocket;

    // The username associated with the client; if this is not
    // set, then only the `createUser` method can be called.
    private static String username = null;

    /**
     * Launch a MessageReceiver thread which is dedicated to receiving
     * messages from the server.
     *
     * @param address       The IP address of the server
     * @return              The connection opened for the MessageReceiver
     * @throws IOException  Thrown on any network exception
     */
    public static Connection launchMessageReceiver(String address) throws IOException {
        messageSocket = new Socket(address, Constants.MESSAGE_PORT);
        Connection connection = new Connection(messageSocket);
        MessageReceiver receiver = new MessageReceiver(connection);
        new Thread(receiver).start();

        return connection;
    }

    /**
     * Handles the main loop of the client which includes asking for the
     * user for the API call that would like to call, then sending a
     * message to the server and fetching a response.
     * @param args  Unused
     */
    public static void main(String[] args) {
        Scanner inputReader = new Scanner(System.in);

        // Get server IP address from user.
        System.out.println("Enter the IP address of the server (leave blank for `localhost`).");
        String address = inputReader.nextLine();
        if (address == "") {
            address = "localhost";
        }

        try {
            Socket socket = new Socket(address, Constants.API_PORT);
            Logging.logInfo("Connection established to " + address);

            connection = new Connection(socket);
            ClientCore client = new ClientCore();

            String options = "Pick an option:\n" +
                    "0. Exit (and log-out).\n" +
                    "1. Create an account (and log-in). You must supply a unique user name (case-sensitive).\n" +
                    "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                    "3. Send a message to a recipient.\n" +
                    "4. Deliver undelivered messages to a particular user.\n" +
                    "5. Delete an account.\n" +
                    "6. Log in to an existing account.";
            int choice = -1;
            while (true) {
                System.out.println(options);

                // Get desired API call from user
                API method;
                try {
                    choice = Integer.parseInt(inputReader.nextLine());
                    method = API.fromInt(choice);
                    Logging.logService("You have chosen option: " + method.toString());
                } catch (NumberFormatException e) {
                    // Invalid choice selected.
                    Logging.logService("Option must be an integer (between 0 and 6).");
                    continue;
                }

                String username = client.getUsername();
                if (username == null) {
                    // The user should only be allowed to select a method
                    // `CREATE_ACCOUNT` or `LOGIN` if the username is not set.
                    if (method != API.CREATE_ACCOUNT && method != API.LOGIN) {
                        Logging.logService("Please first create a username or log in, by selecting option "
                                + API.CREATE_ACCOUNT.getIdentifier() + " or " + API.LOGIN.getIdentifier());
                    } else {
                        LoginRequest loginRequest;
                        StatusMessageResponse statusResponse;
                        if (method == API.CREATE_ACCOUNT) {
                            Logging.logService("Pick your username.");
                            String localUsername = inputReader.nextLine();
                            CreateAccountRequest request = new CreateAccountRequest(localUsername);
                            request.genGenericRequest().writeToStream(connection);
                            Response responses = Response.genResponse(connection);
                            responses.printResponses();

                            loginRequest = new LoginRequest(localUsername);
                            statusResponse = new StatusMessageResponse(responses);
                        } else {
                            // We always have method == API.LOGIN in this case.
                            Logging.logService("Select the username.");
                            username = inputReader.nextLine();

                            Logging.logService("Attempting to log in...");
                            loginRequest = new LoginRequest(username);
                            loginRequest.genGenericRequest().writeToStream(connection);
                            Response response = Response.genResponse(connection);
                            response.printResponses();

                            statusResponse = new StatusMessageResponse(response);
                        }

                        // Set status to logged in.
                        client.loginAPI(loginRequest, statusResponse);
                        // Begin mesage receiver
                        messageConnection = launchMessageReceiver(address);
                    }
                } else {
                    // Username is already set and the user is logged in.
                    if (method == API.CREATE_ACCOUNT || method == API.LOGIN) {
                        Logging.logService("You are already logged in as " + username + ". " +
                                "You may only create an account or login from a un-logged-in state.");
                    } else if (method == API.LOGOUT) {
                        Logging.logService("Logging out of the account associated to the username: " +
                                client.getUsername());

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

                    } else if (method == API.GET_ACCOUNTS){
                        String text_wildcard = "";
                        Logging.logService("Optionally, specify a text (regex) wildcard. Else press enter.");
                        text_wildcard = inputReader.nextLine();

                        if(text_wildcard.equals("")){
                            System.out.println("Proceeding with no wildcard.");
                        } else{
                            System.out.println("Text wildcard: " + text_wildcard);
                        }
                        
                        // Make sure to handle case of text wildcard empty (search everything) or nonempty.
                        // And are there text wildcards we would disallow or that could cause issues?
                        GetAccountsRequest request = new GetAccountsRequest(text_wildcard);
                        request.genGenericRequest().writeToStream(connection);
                        Response responses = Response.genResponse(connection);
                        responses.printResponses();
                    } else if (method == API.SEND_MESSAGE) {
                        // Do we need to handle case of empty message?
                        String recipient = "";
                        String message = "";
                        Logging.logService("Pick your recipient.");
                        recipient = inputReader.nextLine();
                        // Maybe check with server if it's a real recipient first?

                        Logging.logService("Specify your message.");
                        message = inputReader.nextLine();

                        SendMessageRequest request = new SendMessageRequest(username, recipient, message);
                        request.genGenericRequest().writeToStream(connection);
                        Response responses = Response.genResponse(connection);
                        for (String response : responses.getResponses()) {
                            System.out.println("[RESPONSE] " + response);
                        }
                    } else if (method == API.GET_UNDELIVERED_MESSAGES){
                        Logging.logService("Delivering undelivered messages to: " + username);
                        GetUndeliveredMessagesRequest request = new GetUndeliveredMessagesRequest(username);
                        request.genGenericRequest().writeToStream(connection);
                        Response responses = Response.genResponse(connection);
                        responses.printResponses();
                    } else if (method ==API.DELETE_ACCOUNT) {
                        System.out.println("Deleting the account associated to the username: " + username);
                        DeleteAccountRequest request = new DeleteAccountRequest(username);
                        request.genGenericRequest().writeToStream(connection);
                        Response responses = Response.genResponse(connection);
                        responses.printResponses();

                        StatusMessageResponse statusResponses = new StatusMessageResponse(responses);
                        client.logoutAPI(statusResponses);
                    }
                }
            }

            // Close connections before terminating.
            if (messageConnection != null) {
                messageConnection.close();
            }
            connection.close();
        } catch(IOException e) {
            Logging.logInfo("Connection closed.");
        } catch(APIException e) {
            Logging.logInfo("Service exception encountered: " + e.getMessage());
        }
    }

    /**
     * The class which handles receiving messages from a server.
     * Initialized with a Connection, which is then used to listen
     * for messages.
     */
    public static class MessageReceiver implements Runnable {
        private final Connection connection;
        public MessageReceiver(Connection connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                while (true) {
                    Message message = Message.genMessage(connection);
                    Logging.logInfo(message.toString());
                }
            } catch (IOException e) {
                Logging.logInfo("Message receiver service closed (likely due to loss of connection to server).");
            }
        }
    }
}