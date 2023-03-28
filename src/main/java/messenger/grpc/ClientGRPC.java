package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.api.API;
import messenger.api.DisconnectException;
import messenger.network.NetworkUtil;
import messenger.network.PulseCheck;
import messenger.util.BreakableInputReader;
import messenger.util.Constants;
import messenger.util.GRPCUtil;
import messenger.util.Logging;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientGRPC {
    public final MessengerGrpc.MessengerBlockingStub blockingStub;
    public final ClientCore core;
    public final Logging logger;

    private static List<ClientGRPC> clientInstances = new ArrayList<>();

    public ClientGRPC(ManagedChannel channel) {
        core = new ClientCore(channel);
        // Initialize stub which makes API calls.
        blockingStub = MessengerGrpc.newBlockingStub(channel);
        logger = new Logging(core);
    }

    /**
     * Starts a service which allows the client to receive messages from the server.
     * @param port          The port on which to start the receiver
     * @throws IOException  Thrown on network exception.
     */
    private static Server startMessageReceiver(int port) throws IOException {
        Server server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new MessageReceiverImpl())
                .build()
                .start();
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
     * Attempt a handshake with the associated server. Returns whether
     * the server is responsive.
     *
     * @return  True if the server is responsive, false otherwise
     */
    public Boolean attemptHandshake() {
        try {
            blockingStub.handshake(HandshakeRequest.newBuilder().build());
        } catch (Exception ex) {
            // Got a network exception
            return false;
        }
        return core.getConnectionStatus();
    }

    /**
     * Implements API call to create an account.
     * @param username  The username to associate to this client.
     * @return Boolean  Success of request
     */
    public Boolean createAccount(String username) {
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setUsername(username)
                .build();
        LoginReply response;
        try {
            response = blockingStub.createAccount(request);
            logger.logServiceWithContext(response.getStatus().getMessage());
            return response.getStatus().getSuccess();
        } catch (StatusRuntimeException e) {
            logger.logInfoWithContext("RPC failed: " + e.getStatus());
            return false;
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
            logger.logServiceWithContext(response.getStatus().getMessage());
            core.setLoggedOutStatus(response);
        } catch (StatusRuntimeException e) {
            logger.logInfoWithContext("RPC failed: " + e.getStatus());
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

            logger.logServiceWithContext("Found accounts: ");
            for (String account : response.getAccountsList()) {
                logger.logServiceWithContext("\t" + account);
            }
        } catch (StatusRuntimeException e) {
            logger.logInfoWithContext("RPC failed: " + e.getStatus());
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
            e.printStackTrace();
            logger.logInfoWithContext("RPC failed: " + e.getStatus());
            return;
        }
        for (Message message : response.getMessagesList()) {
            GRPCUtil.printMessage(logger, message);
        }
    }

    /**
     * Implements API call to send a specified message from a sender to a recipient
     * @param sender The sender of the message
     * @param recipient The recipient of the message
     * @param message The message to send
     * @return Boolean  Whether the message was queued or not.
     */
    public Boolean sendMessage(String sender, String recipient, String message) {
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
            logger.logServiceWithContext(response.getStatus().getMessage());
            return response.getStatus().getMessage().contains("queued");
        } catch (StatusRuntimeException e) {
            logger.logInfoWithContext("RPC failed: " + e.getStatus());
            return false;
        }
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
            if (response.getStatus().getSuccess()) {
                core.setLoggedInStatus(username, response);
                return response.getReceiverPort();
            } else {
                Logging.logService(response.getStatus().getMessage());
                return -1;
            }
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return -1;
        }
    }

    public Boolean isPrimary() {
        return core.isPrimary();
    }

    public void setPrimary(Boolean primary) {
        core.setPrimary(primary);
        if (primary) {
            blockingStub.markAsPrimary(SetPrimaryRequest.newBuilder().build());
        } else {
            core.setLoggedOutStatus();
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

    public void shutdown() throws InterruptedException {
        core.setExit();
        core.getChannel().shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        BreakableInputReader inputReader = new BreakableInputReader();

        // Get server IP address from user.
        int primaryOffset = 0;
        for (int i = 0; i < 3; i++) {
            System.out.println("Enter IP address for server " + (i + 1) + " or leave line blank for `localhost`.");
            String address = inputReader.nextLine();
            if (address == "") {
                address = "localhost";
            }
            // The port is assumed to be API_PORT + it's order as given to the client
            String destAddress = String.format("%s:%d", address, Constants.API_PORT + i);

            // Start pulsecheck for each server
            ManagedChannel channel = Grpc.newChannelBuilder(destAddress, InsecureChannelCredentials.create()).build();
            ClientGRPC client = new ClientGRPC(channel);
            HandshakeResponse resp = client.blockingStub.handshake(HandshakeRequest.newBuilder().build());
            client.core.setConnected();

            new Thread(new PulseCheck(client)).start();

            if (resp.getIsPrimary()) {
                primaryOffset = i;
            }
            clientInstances.add(client);
        }

        Logging.logService("Server " + primaryOffset + " set as primary.");
        // Input reader is breakable only for the primary client
        ClientGRPC primaryClient = clientInstances.get(primaryOffset);
        primaryClient.core.setPrimary(true);
        inputReader.setClientCore(primaryClient.core);

        String options = "Pick an option:\n" +
                "0. Exit (and log-out).\n" +
                "1. Create an account (and log-in). You must supply a unique user name (case-sensitive).\n" +
                "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
                "3. Send a message to a recipient.\n" +
                "4. Deliver undelivered messages to a particular user.\n" +
                "5. Delete an account (and delete all undelivered messages).\n" +
                "6. Log in to an existing account.";
        int choice = -1;

        Server messageServer = null;
        try {
            while (true) {
                try {
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

                    String username = primaryClient.core.getUsername();
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
                                String localUsername = inputReader.nextLine();
                                if (!primaryClient.createAccount(localUsername)) {
                                    continue;
                                }
                                port = primaryClient.login(localUsername);

                                // Forward account creation
                                for (ClientGRPC client : clientInstances) {
                                    if (client != primaryClient && client.core.getConnectionStatus()) {
                                        client.createAccount(localUsername);
                                    }
                                }
                            } else {
                                Logging.logService("Select the username.");
                                port = primaryClient.login(inputReader.nextLine());
                            }
                            // Start the service to receive messages
                            if (port > 0) {
                                messageServer = startMessageReceiver(port);
                            }
                        }
                    } else {
                        // Username is already set and the user is logged in.
                        if (method == API.CREATE_ACCOUNT || method == API.LOGIN) {
                            Logging.logService("You are already logged in as " + username + ". " +
                                    "You may only create an account or login from a un-logged-in state.");
                        } else if (method == API.LOGOUT) {
                            String localUsername = primaryClient.core.getUsername();
                            Logging.logService("Logging out of the account associated to the username: " +
                                    localUsername);

                            primaryClient.logout(localUsername);
                            break;
                        } else if (method == API.GET_ACCOUNTS) {
                            String text_wildcard = "";
                            Logging.logService("Optionally, specify a text (regex) wildcard. Else press enter.");
                            text_wildcard = inputReader.nextLine();

                            if (text_wildcard.equals("")) {
                                System.out.println("Proceeding with no wildcard.");
                            } else {
                                System.out.println("Text wildcard: " + text_wildcard);
                            }

                            primaryClient.getAccounts(text_wildcard);
                        } else if (method == API.SEND_MESSAGE) {
                            String recipient = "";
                            String message = "";
                            Logging.logService("Pick your recipient.");
                            recipient = inputReader.nextLine();

                            Logging.logService("Specify your message.");
                            message = inputReader.nextLine();

                            // Forward undelivered messages
                            if (primaryClient.sendMessage(username, recipient, message)) {
                                for (ClientGRPC client : clientInstances) {
                                    if (client != primaryClient && client.core.getConnectionStatus()) {
                                        client.sendMessage(username, recipient, message);
                                    }
                                }
                            }
                        } else if (method == API.GET_UNDELIVERED_MESSAGES) {
                            Logging.logService("Delivering undelivered messages to: " + username);
                            primaryClient.getUndeliveredMessages(username);

                            for (ClientGRPC client : clientInstances) {
                                if (client != primaryClient && client.core.getConnectionStatus()) {
                                    client.getUndeliveredMessages(username);
                                }
                            }
                        } else if (method == API.DELETE_ACCOUNT) {
                            Logging.logService("Deleting the account associated to the username: " + username);
                            primaryClient.deleteAccount(username);

                            for (ClientGRPC client : clientInstances) {
                                if (client != primaryClient && client.core.getConnectionStatus()) {
                                    client.deleteAccount(username);
                                }
                            }
                            break;
                        }
                    }
                } catch (DisconnectException ex) {
                    if (messageServer != null) {
                        messageServer.shutdownNow();
                    }
                    primaryClient.setPrimary(false);

                    Logging.logService("Will attempt " + Constants.CLIENT_TIMEOUT +
                            " times to restablish connection...");
                    for (int i = 0; i < Constants.CLIENT_TIMEOUT; i++) {
                        Thread.sleep(500);

                        // offset should wrap back around to 0 after 2.
                        primaryOffset = (primaryOffset + 1) % 3;
                        Logging.logService("Trying connection to server " + primaryOffset);

                        // Attempt handshake
                        ClientGRPC client = clientInstances.get(primaryOffset);
                        if (client.attemptHandshake()) {
                            Logging.logService("Connection restablished. Please log in again to continue.");
                            primaryClient = client;
                            primaryClient.setPrimary(true);
                            inputReader.setClientCore(primaryClient.core);

                            break;
                        } else {
                            Logging.logService("Failed to connect.");
                        }
                    }
                    if (!primaryClient.isPrimary()) {
                        Logging.logService("Last attempt failed, terminating...");
                        break;
                    }
                }
            }
        } finally {
            inputReader.close();
            if (messageServer != null) {
                messageServer.shutdownNow();
            }
            for (ClientGRPC client : clientInstances) {
                client.shutdown();
            }
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
