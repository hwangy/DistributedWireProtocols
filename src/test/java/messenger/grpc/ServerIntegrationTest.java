package messenger.grpc;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

public class ServerIntegrationTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * To test the server, make calls with a real stub using the in-process channel, and verify
     * behaviors or state changes from the client side.
     */
    @Test
    public void greeterImpl_replyMessage() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        ServerCore core = new ServerCore();
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new ServerGRPC.MessageServerImpl(core))
                .build().start());

        MessengerGrpc.MessengerBlockingStub blockingStub = MessengerGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));


        LoginReply reply =
                blockingStub.createAccount(CreateAccountRequest.newBuilder().setUsername("test name").build());
        Assert.assertTrue(true);
    }
}
