package messenger;

import messenger.objects.request.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientCore {
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;
    public ClientCore(DataOutputStream outputStream, DataInputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public void sendRequest(Request request) throws IOException {
        request.writeToStream(outputStream);
    }

    public void closeConnections() throws IOException {
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
}
