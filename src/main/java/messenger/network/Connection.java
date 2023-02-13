package messenger.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Manages an input / output DataStream connection. This is designed
 * to be modular so that it can be easily mocked for integration tests.
 */
public class Connection {
    private final Socket clientSocket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    /**
     * Generates a Connection object from a socket.
     *
     * @param socket        Server socket to connect to.
     * @throws IOException  Handles network exception
     */
    public Connection(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Cleans up connections.
     *
     * @throws IOException  Handles network exception
     */
    public void close() throws IOException {
        if (inputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
        if (outputStream != null) inputStream.close();
        clientSocket.close();
    }

    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    public void writeInt(int toSend) throws IOException {
        outputStream.writeInt(toSend);
    }

    /**
     * Reads a Java String from the input connection, using the UTF
     * format.
     *
     * @return  A string read from input connection.
     * @throws  IOException
     */
    public String readString() throws IOException {
        return inputStream.readUTF();
    }

    public void writeString(String toSend) throws IOException {
        outputStream.writeUTF(toSend);
    }

    public void flushOutput() throws IOException {
        outputStream.flush();
    }

}
