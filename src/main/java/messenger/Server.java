package messenger;
import messenger.network.Connection;
import messenger.api.API;
import messenger.api.APIException;
import messenger.objects.Message;
import messenger.objects.request.*;
import messenger.objects.response.MethodResponseInterface;
import messenger.util.Constants;
import messenger.util.Logging;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Optional;

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
        ServerSocket messageSocket = null;

        try {
            Logging.logInfo("Starting server...");
            // Socket for API calls
            serverSocket = new ServerSocket(Constants.API_PORT);
            // Socket for sending messages
            messageSocket = new ServerSocket(Constants.MESSAGE_PORT);

            // Both should be resuable.
            serverSocket.setReuseAddress(true);
            messageSocket.setReuseAddress(true);

            Logging.logInfo("Waiting for connection...");
            while(true) {
                Socket socket = serverSocket.accept();
                Logging.logInfo("New connection established " + socket.getInetAddress().getHostAddress());
                Connection connection = new Connection(socket);
                ClientHandler clientHandler = new ClientHandler(connection, server, messageSocket);
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
        private final ServerSocket messageSocket;
        private final Connection connection;

        private String username = null;

        /**
         * Create a ClientHandler on a given Connection and ServerCore.
         *
         * @param connection    The connection this handler reads from and writes to.
         * @param server        The object used to update the state of the server.
         */
        public ClientHandler(Connection connection, ServerCore server, ServerSocket messageSocket) {
            this.connection = connection;
            this.server = server;
            this.messageSocket = messageSocket;
        }

        /**
         * Launches a `MessageHandler` thread which manages sending users
         * addressed towards the logged-in user.
         * @param username      The username to be associated with handler.
         * @throws IOException  Thrown if the connection fails for any reason.
         */
        public void launchMessageDispatcher(String username) throws IOException {
            // If no message socket has been set, immediately return.
            if (messageSocket == null) {
                return;
            }

            Socket socket = messageSocket.accept();//establishes connection
            Connection connection = new Connection(socket);
            MessageHandler messageHandler = new MessageHandler(connection, server, username);
            new Thread(messageHandler).start();
        }

        /**
         * The run method contains the logic of what APIs are called and what request and response objects are created
         * corresponding to each of the method calls.
         */
        public void run() {
            try {
                while (true) {
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

                        // Set username and logged in status
                        username = createAccountRequest.getUsername();
                    } else if (calledMethod == API.DELETE_ACCOUNT) {
                        DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest(request);
                        response = server.deleteAccountAPI(deleteAccountRequest);

                        username = null;
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
                    } else if (calledMethod == API.LOGIN) {
                        LoginRequest loginRequest = new LoginRequest(request);
                        response = server.loginUserAPI(loginRequest);

                        username = loginRequest.getUsername();
                    } else if (calledMethod == API.LOGOUT) {
                        LogoutRequest logoutRequest = new LogoutRequest(request);
                        response = server.logoutUserAPI(logoutRequest);

                        username = null;
                    }
                    if (response != null) {
                        Logging.logService(response.getStringStatus());
                        response.genGenericResponse().writeToStream(connection);

                        if ((calledMethod == API.CREATE_ACCOUNT || calledMethod == API.LOGIN) && username != null) {
                            launchMessageDispatcher(username);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Connection to client lost.
                Logging.logInfo("Connection closed");
            } catch (APIException e) {
                Logging.logInfo("API Exception encountered (" +
                        e.getMessage() + "). Dropping connection");
            } finally {
                // Always try to log out the user if the client handler is
                // terminated for any reason. Additionally try to clean up
                // the connection.
                try {
                    if (username != null) server.logoutUser(username);
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Each client may have at most one MessageHandler, which allows
         * the client to receive the messages directed towards their
         * username. A MessageHandler is created only after a client logs in.
         */
        public static class MessageHandler implements Runnable {

            private final Connection connection;
            private final ServerCore server;
            private final String username;

            public MessageHandler(Connection connection, ServerCore server, String username) {
                this.connection = connection;
                this.server = server;
                this.username = username;
            }

            public void run() {
                while (true) {
                    try {
                        // Only execute this thread once every second.
                        Thread.sleep(1000);

                        // If the user is no longer logged in,
                        if (!server.isLoggedIn(username)) {
                            connection.close();
                            return;
                        }

                        Optional<List<Message>> messageList = server.getQueuedMessages(username);
                        if (messageList.isPresent()) {
                            for (Message message : messageList.get()) {
                                message.writeToStream(connection);
                            }
                            Logging.logInfo("All messages delivered to user " + username);
                            server.unqueueMessages(username);
                        }
                    } catch(Exception ex){
                        ex.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}