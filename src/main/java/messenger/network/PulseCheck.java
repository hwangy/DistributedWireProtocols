package messenger.network;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import messenger.grpc.ClientCore;
import messenger.grpc.ClientGRPC;
import messenger.grpc.HandshakeRequest;
import messenger.util.Logging;

import java.io.IOException;

public class PulseCheck implements Runnable {
    private final ClientCore core;
    private final ClientGRPC client;

    public PulseCheck(ClientGRPC client) {
        this.client = client;
        this.core = client.core;
        core.setConnected();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                if (core.getExit()) break;
                if (!core.getConnectionStatus()) {
                    // Attempt handshake
                    try {
                        client.blockingStub.handshake(HandshakeRequest.newBuilder().build());
                        if (core.isChannelReady()) {
                            core.setConnected();
                        }
                    } catch (Exception ex) {
                        continue;
                    }
                } else if (!core.isChannelReady()) {
                    core.setDisconnected();
                    if (core.isPrimary()) {
                        Logging.logService("The server has been disconnected! Press enter to reconnect.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }
}
