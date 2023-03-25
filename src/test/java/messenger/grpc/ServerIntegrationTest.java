package messenger.grpc;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class ServerIntegrationTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * Basic integration test which ensures a request from the client is
     * successfully processed by the server.
     */
    @Test
    public void testEndToEndBehavior() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        ServerCore core = new ServerCore(0, new ArrayList<>());
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new ServerGRPC.MessageServerImpl(core))
                .build().start());

        MessengerGrpc.MessengerBlockingStub blockingStub = MessengerGrpc.newBlockingStub(
                // Create a client channel and register for automatic graceful shutdown.
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));


        LoginReply reply =
                blockingStub.createAccount(CreateAccountRequest.newBuilder().setUsername("test name").build());
        Assert.assertTrue(reply.getStatus().getSuccess());
    }
}
