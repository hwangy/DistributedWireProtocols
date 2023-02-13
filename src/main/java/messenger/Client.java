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

            String options = "Pick a method:\n" +
                    "1. Create an account. You must supply a unique user name.\n" +
                    "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                    "3. Send a message to a recipient.\n" +
                    "4. Deliver undelivered messages to a particular user.\n" +
                    "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)";


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

                // Note: This doesn't work yet! I can't get the Client to wait for the Scanner before proceeding.
                Logging.logDebug("In scanner component");
                String line;
                while(inputReader.hasNextLine()) {
                    line = inputReader.nextLine();
                    Logging.logDebug("Read \"" + line + "\"");
                }


                /*
                choice = inputReader.nextInt();
                if (choice < 1|| choice > 6) {
                    // throw an exception
                }*/

                choice = 1;
                API method = API.fromInt(choice);
                if (method == API.CREATE_ACCOUNT) {
                    String username = "test";
                    Logging.logDebug("Testing create user");
                    CreateAccountRequest request = new CreateAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    Response responses = Response.genResponse(connection);
                    for (String response : responses.getResponses()) {
                        System.out.println("[RESPONSE] " + response);
                    }
                    // Get response
                } else if (method == API.GET_ACCOUNTS){
                    String text_wildcard = "test";
                    Logging.logDebug("Test get accounts");
                    GetAccountsRequest request = new GetAccountsRequest(text_wildcard);
                    request.genGenericRequest().writeToStream(connection);
                    // Wait for response and handle response
                } else if (method == API.SEND_MESSAGE) {
                    String recipient = "test";
                    Logging.logDebug("Test send message");
                    SendMessageRequest request = new SendMessageRequest(recipient);
                    request.genGenericRequest().writeToStream(connection);
                    // Wait for response and handle response
                } else if (method == API.GET_UNDELIVERED_MESSAGES){
                    String username = "test";
                    Logging.logDebug("Test get undelivered messages");
                    GetUndeliveredMessagesRequest request = new GetUndeliveredMessagesRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    // Wait for response and handle response
                } else if (method ==API.DELETE_ACCOUNT) {
                    // Question: If the user is logged in, do we want to ask for username?
                    String username = "test";
                    Logging.logDebug("Test delete account");
                    DeleteAccountRequest request = new DeleteAccountRequest(username);
                    request.genGenericRequest().writeToStream(connection);
                    // Wait for response and handle response
                }

                break;
            }
            connection.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }  
}