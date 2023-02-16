package messenger.grpc;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import messenger.ClientCore;
import messenger.api.API;
import messenger.objects.request.LoginRequest;
import messenger.objects.response.StatusMessageResponse;
import messenger.util.Constants;
import messenger.util.Logging;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ClientGRPC {
    private final MessengerGrpc.MessengerBlockingStub blockingStub;
    private int identifier = -1;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public ClientGRPC(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = MessengerGrpc.newBlockingStub(channel);
    }

    /** Say hello to server. */
    public void createAccount(String username) {
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setUsername(username)
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

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
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
}
