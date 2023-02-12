package messenger.objects.request;

import messenger.network.Connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private final int methodId;
    private final List<String> arguments;

    public Request(int methodId, List<String> arguments) {
        this.methodId = methodId;
        this.arguments = arguments;
    }

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


    public int getMethodId() {
        return this.methodId;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public void writeToStream(Connection connection) throws IOException {
        connection.writeInt(methodId);
        connection.writeInt(arguments.size());
        for (String argument : arguments) {
            connection.writeString(argument);
        }
        connection.flushOutput();
    }
}
