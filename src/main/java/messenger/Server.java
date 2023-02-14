package messenger;
import messenger.network.Connection;
import messenger.api.API;
import messenger.api.APIException;
import messenger.objects.request.*;
import messenger.objects.response.MethodResponseInterface;
import messenger.util.Logging;

import java.io.*;
import java.net.*;

/**
 * Multithreaded server implementation, loosely based on
 * https://www.geeksforgeeks.org/multithreaded-servers-in-java/
 *
 * For each connection, the server creates a ClientHandler instance in a separate
 * thread.
 */
public class Server {
    public static void main(String[] args){
        ServerCore server = new ServerCore();
        ServerSocket serverSocket = null;

        try {
            Logging.logInfo("Starting server...");
            serverSocket=new ServerSocket(6666);
            serverSocket.setReuseAddress(true);
            Logging.logInfo("Waiting for connection...");

            while(true) {
                Socket socket = serverSocket.accept();//establishes connection
                Logging.logInfo("New connection established " + socket.getInetAddress().getHostAddress());
                Connection connection = new Connection(socket);
                ClientHandler clientHandler = new ClientHandler(connection, server);
                new Thread(clientHandler).start();
            }
        } catch(Exception e) {
            System.out.println(e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * The ClientHandler class is created per-connected user and handles requests
     * from that client. It maintains a handle to a `ServerCore` object (shared)
     * by all the ClientHandlers, so that they can all update the same state.
     *
     * Concurrency safety is not ensured for `ServerCore`.
     */
    public static class ClientHandler implements Runnable {
        private final ServerCore server;

        private final Connection connection;
        // Constructor

        /**
         * Create a ClientHandler on a given Connection and ServerCore.
         *
         * @param connection    The connection this handler reads from and writes to.
         * @param server        The object used to update the state of the server.
         */
        public ClientHandler(Connection connection, ServerCore server)
        {
            this.connection = connection;
            this.server = server;
        }

        public void run() {
            try {
                while (true) {
                    /*
                    1. Read input as a string
                    2. Convert string input into a Request
                    3. Depending on first field of Request, enter appropriate IF statement
                    IF:
                        4. request = [method]Request.parseGenericRequest(request);
                        5. [Method]Response = [method](request)
                    6. String rawResponse = encResponse([Method]Response.getGenericResponse());
                    7. sendToClient(rawResponse)
                    */

                    try {
                        // Form request from input stream
                        Request request = Request.genRequest(connection);
                        // Response object to populate.
                        MethodResponseInterface response = null;

                        // Parse integer into the corresponding API call
                        API calledMethod = API.fromInt(request.getMethodId());
                        Logging.logInfo("Processing API call " + calledMethod.toString());
                        if (calledMethod == API.CREATE_ACCOUNT) {
                            // Client wants to create a new user
                            CreateAccountRequest createAccountRequest = new CreateAccountRequest(request);
                            response = server.createAccountAPI(createAccountRequest);
                        } else if (calledMethod == API.DELETE_ACCOUNT) {
                            DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest(request);
                            response = server.deleteAccountAPI(deleteAccountRequest);
                        } else if (calledMethod == API.GET_ACCOUNTS) {
                            GetAccountsRequest getAccountsRequest = new GetAccountsRequest(request);
                            response = server.getAccountsAPI(getAccountsRequest);
                        } else if (calledMethod == API.GET_UNDELIVERED_MESSAGES) {
                            GetUndeliveredMessagesRequest getUndeliveredMessagesRequest =
                                    new GetUndeliveredMessagesRequest(request);
                            response = server.getUndeliveredMessagesAPI(getUndeliveredMessagesRequest);
                        } else if (calledMethod == API.SEND_MESSAGE) {
                            SendMessageRequest sendMessageRequest = new SendMessageRequest(request);
                            response = server.sendMessageAPI(sendMessageRequest);
                        }
                        if (response != null) {
                            Logging.logService(response.getStringStatus());
                            Logging.logInfo("Sending response to client");
                            response.genGenericResponse().writeToStream(connection);
                        }
                    } catch (EOFException e) {
                        Logging.logInfo("Connection closed");
                        break;
                    } catch (APIException e) {
                        //TODO: Return API exceptions to the client.
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}