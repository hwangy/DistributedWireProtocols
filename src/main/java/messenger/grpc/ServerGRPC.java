package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.util.Constants;
import messenger.util.Logging;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implements the Message Service API
 */
public class ServerGRPC {
    private Server server;

    /**
     * Start the Message server.
     * @throws IOException  Thrown on network exception
     */
    private void start() throws IOException {
        ServerCore core = new ServerCore();
        /* The port on which the server should run */
        server = Grpc.newServerBuilderForPort(Constants.API_PORT, InsecureServerCredentials.create())
                .addService(new MessageServerImpl(core))
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ServerGRPC.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerGRPC server = new ServerGRPC();
        server.start();
        server.blockUntilShutdown();
    }

    /**
     * The implementation of the Message server. The server handles the main
     * API-related calls.
     */
    static class MessageServerImpl extends MessengerGrpc.MessengerImplBase {

        private final ServerCore core;

        public MessageServerImpl(ServerCore core) {
            this.core = core;
        }

        /**
         * Creates an account. Since this also logs in the user, it also
         * launches a MessageHandler thread to dispatch messages to that
         * user.
         * @param req               A CreateAccountRequest
         * @param responseObserver  Observer on which to send responses
         */
        @Override
        public void createAccount(CreateAccountRequest req, StreamObserver<LoginReply> responseObserver) {
            // Logic here for processing request.
            Logging.logInfo("Received CREATE_ACCOUNT request");
            responseObserver.onNext(core.createAccountAPI(req));
            MessageHandler handler = new MessageHandler(core, req.getUsername(), req.getIpAddress());
            new Thread(handler).start();
            responseObserver.onCompleted();
        }
    }

    /**
     * Each client may have at most one MessageHandler, which allows
     * the client to receive the messages directed towards their
     * username. A MessageHandler is created only after a client logs in.
     */
    public static class MessageHandler implements Runnable {

        private final ServerCore server;
        private final String username;

        private final MessageReceiverGrpc.MessageReceiverBlockingStub blockingStub;

        public MessageHandler(ServerCore server, String username, String ipAddress) {
            this.server = server;
            this.username = username;

            String target = String.format("%s:%d", ipAddress, Constants.MESSAGE_PORT);
            ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                    .build();
            blockingStub = MessageReceiverGrpc.newBlockingStub(channel);
        }

        /**
         * Sends a message over the network.
         * @param message   The message to be sent.
         */
        public void sendMessage(Message message) {
            Status status = blockingStub.sendMessage(message).getStatus();

            if (status.getSuccess()) {
                Logging.logInfo("Message successfully dispatched to user " + username);
            } else {
                Logging.logInfo("Message failed to send to " + username);
            }
            // TODO: Populate the sent-timestamp
        }

        /**
         * The main loop of the MessageHandler. The handler routinely checks the list of
         * queued messages, then if one is found which is addressed to the current user,
         * it is sent using the `sendMessage` API call.
         */
        public void run() {
            Logging.logInfo("Message handler for user " + username + " started successfully.");

            while (true) {
                try {
                    // Only execute this thread once every second.
                    Thread.sleep(1000);

                    // If the user is no longer logged in,
                    if (!server.isLoggedIn(username)) {
                        return;
                    }

                    Optional<List<messenger.objects.Message>> messageList = server.getQueuedMessages(username);
                    if (messageList.isPresent()) {
                        for (messenger.objects.Message message : messageList.get()) {
                            sendMessage(Message.newBuilder()
                                    .setMessage(message.getMessage())
                                    .setRecipient(message.getRecepient())
                                    .setSender(message.getSender())
                                    .setSentTimestamp(message.getSentTimestamp())
                                    .build());
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
