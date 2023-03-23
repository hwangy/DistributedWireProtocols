package messenger.util;

import messenger.api.DisconnectException;
import messenger.grpc.ClientCore;

import java.util.Scanner;

/**
 * A wrapper for the standard InputReader that allows us
 * to check the connection status and throw an exception
 * if the server is no longer connected.
 */
public class BreakableInputReader {
    private final Scanner inputReader;
    private ClientCore core = null;

    public BreakableInputReader() {
        this.inputReader = new Scanner(System.in);
    }

    public void setClientCore(ClientCore core) {
        this.core = core;
    }

    public String nextLine() throws DisconnectException {
        String input = inputReader.nextLine();
        if (core != null && !core.getConnectionStatus()) {
            throw new DisconnectException("Server has disconnected.");
        }
        return input;
    }

    public void close() {
        inputReader.close();
    }
}
