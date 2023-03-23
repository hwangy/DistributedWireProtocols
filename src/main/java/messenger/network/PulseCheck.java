package messenger.network;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import messenger.grpc.ClientCore;
import messenger.util.Logging;

public class PulseCheck implements Runnable {
    private final ManagedChannel channel;
    private final ClientCore core;

    public PulseCheck(ClientCore core, ManagedChannel channel) {
        this.channel = channel;
        this.core = core;
        core.setConnected();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                if (core.getExit()) break;
                if (!core.getConnectionStatus()) continue;

                ConnectivityState state = channel.getState(false);
                Logging.logDebug(state.toString());
                if (!state.equals(ConnectivityState.READY)) {
                    core.setDisconnected();
                    Logging.logService("The server has been disconnected! Press enter to reconnect.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }
}
