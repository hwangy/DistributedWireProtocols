package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.api.API;
import messenger.network.NetworkUtil;
import messenger.util.Constants;
import messenger.util.GRPCUtil;
import messenger.util.Logging;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientGRPC {
    private final MessengerGrpc.MessengerBlockingStub blockingStub;
    private final ClientCore core;

    public ClientGRPC(Channel channel) {
        core = new ClientCore();
        // Initialize stub which makes API calls.
        blockingStub = MessengerGrpc.newBlockingStub(channel);
    }

    /**
     * Starts a service which allows the client to receive messages from the server.
     * @param port          The port on which to start the receiver
     * @throws IOException  Thrown on network exception.
     */
    private static Server startMessageReceiver(int port) throws IOException {
        Server server = ServerBuilder.forPort(port)
                .addService(new MessageReceiverImpl()).build().start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                interrupt();
                System.err.println("*** server shut down");
            }
        });
        return server;
    }

    /**
     * Implements API call to create an account. Additionally, this call will provide
     * the server with the client's IP address to facilitate the server forming a
     * client-connection back to this client, in order to send messages addressed to this
     * client.
     * @param username  The username to associate to this client.
     * @return int      The port on which to start the MessageReceiver
     */
    public int createAccount(String username) {
        // Try to fetch the local IP address to provide to server
        String ipAddress = null;
        try {
            Logging.logInfo("Got here.");
            ipAddress = NetworkUtil.getLocalIPAddress();
            Logging.logInfo("Got IP Address: " + ipAddress);
        } catch (UnknownHostException ex) {
            Logging.logInfo("Failed to get local IP address, message handler will NOT be started.");
        }
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setUsername(username)
                .setIpAddress(ipAddress)
                .build();
        LoginReply response;
        try {
            response = blockingStub.createAccount(request);
            // Log in the user
            core.setLoggedInStatus(username, response);
            return response.getReceiverPort();
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return -1;
        }
    }

    /**
     * Implements API call to delete an account. 
     * @param username  The username to associate to this client.
     */
    public void deleteAccount(String username) {
        DeleteAccountRequest request = DeleteAccountRequest.newBuilder()
                .setUsername(username)
                .build();
        StatusReply response;
        try {
            response = blockingStub.deleteAccount(request);
            Logging.logService(response.getStatus().getMessage());
            core.setLoggedOutStatus(response);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
        }
    }

    /**
     * Implements API call to get all accounts associated with the 
     * text wildcard that is input.
     * @param text_wildcard  The text wildcard to use for returning accounts
     */
    public void getAccounts(String text_wildcard) {
        GetAccountsRequest request = GetAccountsRequest.newBuilder()
            .setTextWildcard(text_wildcard)
            .build();
        GetAccountsReply response;
        try {
            response = blockingStub.getAccounts(request);

            Logging.logService("Found accounts: ");
            for (String account : response.getAccountsList()) {
                Logging.logService("\t" + account);
            }
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }
    }

    /**
     * Implements API call to get undelivered methods associated with 
     * the username
     * @param username The username associated to this client.
     */
    public void getUndeliveredMessages(String username) {
        GetUndeliveredMessagesRequest request = GetUndeliveredMessagesRequest.newBuilder()
            .setUsername(username)
            .build();
        GetUndeliveredMessagesReply response;
        try {
            response = blockingStub.getUndeliveredMessages(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }
        for (Message message : response.getMessagesList()) {
            GRPCUtil.printMessage(message);
        }
    }

    /**
     * Implements API call to send a specified message from a sender to a recipient
     * @param sender The sender of the message
     * @param recipient The recipient of the message
     * @param message The message to send
     */
    public void sendMessage(String sender, String recipient, String message) {
        Message message_object = Message.newBuilder()
            .setSentTimestamp(System.currentTimeMillis())
            .setSender(sender)
            .setRecipient(recipient)
            .setMessage(message)
            .build();
        SendMessageRequest request = SendMessageRequest.newBuilder()
            .setMessage(message_object)
            .build();
        StatusReply response;
        try {
            response = blockingStub.sendMessage(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }
        Logging.logService(response.getStatus().getMessage());
    }

     /**
     * Implements API call to log into an account. Additionally, this call will provide
     * the server with the client's IP address to facilitate the server forming a
     * client-connection back to this client, in order to send messages addressed to this
     * client.
     * @param username  The username to associate to this client.
      * @return int      The port on which to start the MessageReceiver
     */
    public int login(String username) {
        // Try to fetch the local IP address to provide to server
        String ipAddress = null;
        try {
            ipAddress = NetworkUtil.getLocalIPAddress();
            Logging.logInfo("Got IP Address: " + ipAddress);
        } catch (UnknownHostException ex) {
            Logging.logInfo("Failed to get local IP address, message handler will NOT be started.");
        }

        LoginRequest request = LoginRequest.newBuilder()
                .setIpAddress(ipAddress)
                .setUsername(username)
                .build();
        LoginReply response;

        try {
            response = blockingStub.login(request);
            core.setLoggedInStatus(username, response);
            return response.getReceiverPort();
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return -1;
        }
    }

     /**
     * Implements API call to log out of an account.
     * @param username  The username associated to this client.
     */
    public void logout(String username) {
        LogoutRequest request = LogoutRequest.newBuilder()
                .setUsername(username)
                .build();
        StatusReply response;

        try {
            response = blockingStub.logout(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }

        // Logout the user
        core.setLoggedOutStatus(response);
    }

    public static void main(String[] args) throws Exception {
        Scanner inputReader = new Scanner(System.in);

        // Get server IP address from user.
        System.out.println("Enter the IP address of the server (leave blank for `localhost`).");
        String address = inputReader.nextLine();
        if (address == "") {
            address = "localhost";
        }
        String target = String.format("%s:%d", address, Constants.API_PORT);

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ManagedChannel channel = ManagedChannelBuilder.forAddress(address, Constants.API_PORT)
                .usePlaintext().build();


        String options = "Pick an option:\n" +
                "0. Exit (and log-out).\n" +
                "1. Create an account (and log-in). You must supply a unique user name (case-sensitive).\n" +
                "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                "3. Send a message to a recipient.\n" +
                "4. Deliver undelivered messages to a particular user.\n" +
                "5. Delete an account.\n" +
                "6. Log in to an existing account.";
        int choice = -1;

        try {
            ClientGRPC client = new ClientGRPC(channel);
            Server server = null;
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

                String username = client.core.getUsername();
                if (username == null) {
                    // The user should only be allowed to select a method
                    // `CREATE_ACCOUNT` or `LOGIN` if the username is not set.
                    if (method != API.CREATE_ACCOUNT && method != API.LOGIN) {
                        Logging.logService("Please first create a username or log in, by selecting option "
                                + API.CREATE_ACCOUNT.getIdentifier() + " or " + API.LOGIN.getIdentifier());
                    } else {
                        int port;
                        if (method == API.CREATE_ACCOUNT) {
                            Logging.logService("Pick your username.");
                            port = client.createAccount(inputReader.nextLine());
                        } else {
                            Logging.logService("Select the username.");
                            port = client.login(inputReader.nextLine());
                        }
                        // Start the service to receive messages
                        if (port > 0) {
                            server = startMessageReceiver(port);
                        }

                    }
                } else {
                    // Username is already set and the user is logged in.
                    if (method == API.CREATE_ACCOUNT || method == API.LOGIN) {
                        Logging.logService("You are already logged in as " + username + ". " +
                                "You may only create an account or login from a un-logged-in state.");
                    } else if (method == API.LOGOUT) {
                        String localUsername = client.core.getUsername();
                        Logging.logService("Logging out of the account associated to the username: " +
                                localUsername);

                        client.logout(localUsername);
                        if (server != null) {
                            server.shutdownNow();
                        }
                        inputReader.close();
                        break;

                    }  else if (method == API.GET_ACCOUNTS){
                        String text_wildcard = "";
                        Logging.logService("Optionally, specify a text (regex) wildcard. Else press enter.");
                        text_wildcard = inputReader.nextLine();

                        if(text_wildcard.equals("")){
                            System.out.println("Proceeding with no wildcard.");
                        } else{
                            System.out.println("Text wildcard: " + text_wildcard);
                        }

                        client.getAccounts(text_wildcard);
                        
                    } else if (method == API.SEND_MESSAGE) {
                        String recipient = "";
                        String message = "";
                        Logging.logService("Pick your recipient.");
                        recipient = inputReader.nextLine();

                        Logging.logService("Specify your message.");
                        message = inputReader.nextLine();
                        client.sendMessage(username, recipient, message);

                    } else if (method == API.GET_UNDELIVERED_MESSAGES){
                        Logging.logService("Delivering undelivered messages to: " + username);
                        client.getUndeliveredMessages(username);

                    } else if (method == API.DELETE_ACCOUNT) {
                        Logging.logService("Deleting the account associated to the username: " + username);
                        client.deleteAccount(username);
                        if (server != null) {
                            server.shutdownNow();
                            server = null;
                        }
                    }
 
                }
            }
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * A simple message receiver implementation that prints out the messages
     * it receives from the Server.
     */
    static class MessageReceiverImpl extends MessageReceiverGrpc.MessageReceiverImplBase {
        @Override
        public void sendMessage(Message req, StreamObserver<StatusReply> responseObserver) {
            GRPCUtil.printMessage(req);
            responseObserver.onNext(GRPCUtil.genSuccessfulReply());
            responseObserver.onCompleted();
        }
    }
}
