package messenger.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import messenger.network.Address;
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
     *
     * The overridden methods in this class are largely uninteresting; see
     * the corresponding `[methodName]API` methods in the `ServerCore` class
     * for implementation details and explanations.
     *
     * As such, comments for the methods are largely omitted, except where
     * something interesting happens (e.g. launching the message handler).
     */
    static class MessageServerImpl extends MessengerGrpc.MessengerImplBase {

        private final ServerCore core;

        public MessageServerImpl(ServerCore core) {
            this.core = core;
        }

        /**
         * The folowing two methods, createAccount and login, also start a MessageHandler
         * service which sends messages received to the user which is logged in.
         */
        @Override
        public void createAccount(CreateAccountRequest req, StreamObserver<LoginReply> responseObserver) {
            LoginReply reply = core.createAccountAPI(req);
            responseObserver.onNext(reply);
            MessageHandler handler = new MessageHandler(core, req.getUsername(),
                    new Address(req.getIpAddress(), reply.getReceiverPort()));
            new Thread(handler).start();
            responseObserver.onCompleted();
        }

        @Override
        public void login(LoginRequest req, StreamObserver<LoginReply> responseObserver) {
            LoginReply reply = core.loginUserAPI(req);
            responseObserver.onNext(reply);
            MessageHandler handler = new MessageHandler(core, req.getUsername(),
                    new Address(req.getIpAddress(), reply.getReceiverPort()));
            new Thread(handler).start();
            responseObserver.onCompleted();
        }


        @Override
        public void sendMessage(SendMessageRequest req, StreamObserver<StatusReply> responseObserver) {
            responseObserver.onNext(core.sendMessageAPI(req));
            responseObserver.onCompleted();
        }

        @Override
        public void getAccounts(GetAccountsRequest req, StreamObserver<GetAccountsReply> responseObserver) {
            responseObserver.onNext(core.getAccountsAPI(req));
            responseObserver.onCompleted();
        }

        @Override
        public void deleteAccount(DeleteAccountRequest req, StreamObserver<StatusReply> responseObserver) {
            responseObserver.onNext(core.deleteAccountAPI(req));
            responseObserver.onCompleted();
        }

        @Override
        public void getUndeliveredMessages(GetUndeliveredMessagesRequest req,
                                           StreamObserver<GetUndeliveredMessagesReply> responseObserver) {
            responseObserver.onNext(core.getUndeliveredMessagesAPI(req));
            responseObserver.onCompleted();
        }

        @Override
        public void logout(LogoutRequest req, StreamObserver<StatusReply> responseObserver) {
            responseObserver.onNext(core.logoutUserAPI(req));
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

        public MessageHandler(ServerCore server, String username, Address address) {
            this.server = server;
            this.username = username;

            ManagedChannel channel = Grpc.newChannelBuilder(address.toString(), InsecureChannelCredentials.create())
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

                    Optional<List<Message>> messageList = server.getQueuedMessages(username);
                    if (messageList.isPresent()) {
                        Long timestamp = System.currentTimeMillis();
                        for (Message message : messageList.get()) {
                            sendMessage(Message.newBuilder().mergeFrom(message)
                                    .setSentTimestamp(timestamp)
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
