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
                Socket s=serverSocket.accept();//establishes connection
                Logging.logInfo("New connection established " + s.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(s, server);
                clientHandler.init();
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

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ServerCore server;

        private Connection connection;
        // Constructor
        public ClientHandler(Socket socket, ServerCore server)
        {
            this.clientSocket = socket;
            this.server = server;
        }

        public void init() throws IOException {
            this.connection = new Connection(clientSocket);
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
                        e.printStackTrace();
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