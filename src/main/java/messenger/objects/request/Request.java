package messenger.objects.request;

import messenger.network.Connection;
import messenger.util.Logging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private final int methodId;
    private final List<String> arguments;

    /**
     * Form a Request object with the given
     * methodID and arguments
     * @param methodID The method ID
     * @param arguments The arguments
     */
    public Request(int methodId, List<String> arguments) {
        this.methodId = methodId;
        this.arguments = arguments;
    }

    /**
     * Form a Request object by reading from a connection.
     * @input connection    A connection object to read from
     */
    public static Request genRequest(Connection connection) throws IOException {
        // Read method identifier
        List<String> arguments = new ArrayList<>();
        int methodId = connection.readInt();
        int numArguments = connection.readInt();
        // All methods currently expect one argument
        while (numArguments > 0) {
            arguments.add(connection.readString());
            numArguments--;

            //TODO: Enforce time out for receiving all arguments
        }
        return new Request(methodId, arguments);
    }


    /**
     * Get the method ID of the request
     */
    public int getMethodId() {
        return this.methodId;
    }

    /**
     * Get the arguments of the request
     */
    public List<String> getArguments() {
        return this.arguments;
    }

    /**
     * Write to stream corresponding to the connection object
     * @input connection    A connection object to write to
     */
    public void writeToStream(Connection connection) throws IOException {
        connection.writeInt(methodId);
        connection.writeInt(arguments.size());
        for (String argument : arguments) {
            connection.writeString(argument);
        }
        connection.flushOutput();
    }
}
