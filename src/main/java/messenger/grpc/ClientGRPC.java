package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.ClientCore;
import messenger.api.API;
import messenger.network.NetworkUtil;
import messenger.objects.request.LoginRequest;
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

    // Identifier is initially set to -1
    private int identifier = -1;

    private static Server server;

    public ClientGRPC(Channel channel) {
        // Initialize stub which makes API calls.
        blockingStub = MessengerGrpc.newBlockingStub(channel);
    }

    /**
     * Starts a service which allows the client to receive messages from the server.
     * @throws IOException  Thrown on network exception.
     */
    private static void startMessageReceiver() throws IOException {
        server = Grpc.newServerBuilderForPort(Constants.MESSAGE_PORT, InsecureServerCredentials.create())
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
            Logging.logInfo("Got IP Addresss: " + ipAddress);
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

        Logging.logService(response.getStatus().getMessage());
        identifier = response.getConnectionId();
        Logging.logService("Setting identifier to " + identifier);
    }

    public static void main(String[] args) throws Exception {
        ClientCore core = new ClientCore();
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

                String username = core.getUsername();
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

                            //TODO: Log in the user
                            // loginRequest = new LoginRequest(localUsername);
                            // statusResponse = new StatusMessageResponse(responses);
                        }
                        // Set status to logged in.
                        // client.loginAPI(loginRequest, statusResponse);
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
            Logging.logInfo(String.format("Received message from [%s] %d:\t%s",
                    req.getSender(),req.getSentTimestamp(),req.getMessage()));
            responseObserver.onNext(GRPCUtil.genSuccessfulReply());
            responseObserver.onCompleted();
        }
    }
}