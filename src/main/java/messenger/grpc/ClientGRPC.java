package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.api.API;
import messenger.network.NetworkUtil;
import messenger.objects.response.StatusMessageResponse;
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
     * @throws IOException  Thrown on network exception.
     */
    private static void startMessageReceiver() throws IOException {
        Grpc.newServerBuilderForPort(Constants.MESSAGE_PORT, InsecureServerCredentials.create())
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
    }

    /**
     * Implements API call to create an account. Additionally, this call will provide
     * the server with the client's IP address to facilitate the server forming a
     * client-connection back to this client, in order to send messages addressed to this
     * client.
     * @param username  The username to associate to this client.
     */
    public void createAccount(String username) {
        // Try to fetch the local IP address to provide to server
        String ipAddress = null;
        try {
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
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }

        // Log in the user
        core.setLoggedInStatus(username, response);
    }

    /**
     * Implements API call to delete an account. 
     * @param username  The username to associate to this client.
     */
    public void deleteAccount(String username) {
        DeleteAccountRequest request = DeleteAccountRequest.newBuilder()
                .setConnectionId(core.getConnectionId())
                .setUsername(username)
                .build();
        StatusReply response;
        try {
            response = blockingStub.deleteAccount(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }

        Logging.logService(response.getStatus().getMessage());
    }

    /**
     * Implements API call to get all accounts associated with the 
     * text wildcard that is input.
     * @param text_wildcard  The text wildcard to use for returning accounts
     */
    public void getAccounts(String text_wildcard) {
        GetAccountsRequest request = GetAccountsRequest.newBuilder()
            .setConnectionId(core.getConnectionId())
            .setTextWildcard(text_wildcard)
            .build();
        GetAccountsReply response;
        try {
            response = blockingStub.getAccounts(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }
        Logging.logService("Found accounts: ");
        for (String account : response.getAccountsList()) {
            Logging.logService("\t" + account);
        }
    }

    /**
     * Implements API call to get undelivered methods associated with 
     * the username
     * @param username The username associated to this client.
     */
    public void getUndeliveredMessages(String username) {
        GetUndeliveredMessagesRequest request = GetUndeliveredMessagesRequest.newBuilder()
            .setConnectionId(core.getConnectionId())
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
            .setConnectionId(core.getConnectionId())
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
     */
    public void login(String username) {
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
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }

        core.setLoggedInStatus(username, response);
    }

     /**
     * Implements API call to log out of an account.
     * @param username  The username associated to this client.
     */
    public void logout(String username) {
        LogoutRequest request = LogoutRequest.newBuilder()
                .setConnectionId(core.getConnectionId())
                .setUsername(username)
                .build();
        StatusReply response;

        try {
            response = blockingStub.logout(request);
        } catch (StatusRuntimeException e) {
            Logging.logInfo("RPC failed: " + e.getStatus());
            return;
        }

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
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();

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
                        // Start the service to receive messages
                        startMessageReceiver();

                        LoginRequest loginRequest;
                        StatusMessageResponse statusResponse;
                        if (method == API.CREATE_ACCOUNT) {
                            Logging.logService("Pick your username.");
                            String localUsername = inputReader.nextLine();
                            client.createAccount(localUsername);
                            //CreateAccountRequest request = CreateAccountRequest.newBuilder().setUsername(localUsername).setIpAddress(address).build();
                           // LoginReply reply = blockingStub.
                            
                            //LoginReply.genResponse(channel);
                            //Logging.logService(reply)
                        
                        } else {
                            Logging.logService("Select the username.");
                            username = inputReader.nextLine();
                            Logging.logService("Attempting to log in...");
                            client.login(username);

                            //LoginReply.genResponse(channel);
                            //loginRequest = LoginRequest.newBuilder().setIpAddress(address).setUsername(username).build();
                            //do the reply
                            
                        }
                        // Set status to logged in.
                        //client.loginAPI(loginRequest, statusResponse);
                        // begin message receiver?
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

                        //LogoutReqest request = LogoutRequest.newBuilder().setConnectionId(1).setUsername(localUsername).build();
                        // responses

                        //client.core.logoutAPI(statusResponses);
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
                        //GetAccountsRequest request = GetAccountsRequest.newBuilder().setTextWildcard(text_wildcard).build();
                        // generate response
                    } else if (method == API.SEND_MESSAGE) {
                        // Do we need to handle case of empty message?
                        String recipient = "";
                        String message = "";
                        Logging.logService("Pick your recipient.");
                        recipient = inputReader.nextLine();

                        Logging.logService("Specify your message.");
                        message = inputReader.nextLine();
                        client.sendMessage(username, recipient, message);
                       
                        
                        //Long timestamp = System.currentTimeMillis();

                        //Message localMessage = Message.newBuilder().setMessage(message)
                        //    .setSentTimestamp(timestamp).setSender(username).setRecipient(recipient).build();
                        //SendMessageRequest request = SendMessageRequest.newBuilder().setConnectionId(1).setMessage(localMessage).build();
                        // responses
                    } else if (method == API.GET_UNDELIVERED_MESSAGES){
                        Logging.logService("Delivering undelivered messages to: " + username);
                        client.getUndeliveredMessages(username);
                        //GetUndeliveredMessagesRequest request = GetUndeliveredMessagesRequest.newBuilder().setConnectionId(1).setUsername(username).build();
                        // do the responses
                    } else if (method == API.DELETE_ACCOUNT) {
                        Logging.logService("Deleting the account associated to the username: " + username);
                        client.deleteAccount(username);
                        // DeleteAccountRequest request = DeleteAccountRequest.newBuilder().setConnectionId(1).setUsername(username).build();
                        // do responses and logout
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
