package messenger.objects.response;

import messenger.network.Connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Response {

    private final Boolean success;
    private final List<String> responses;

    public Response(Boolean success, List<String> responses) {
        this.success = success;
        this.responses = responses;
    }

    public Boolean isSuccessful() {
        return isSuccessful();
    }

    public List<String> getResponses() {
        return responses;
    }

    /**
     * Form a Response object by reading from a connection.
     * @input connection    A connection object to read from
     */
    public static Response genResponse(Connection connection) throws IOException {
        // Read the success bit
        Boolean success = connection.readInt() > 0 ? true : false;
        // Read the number of responses to process
        int numResponses = connection.readInt();
        List<String> responses = new ArrayList<String>();
        while (numResponses > 0) {
            responses.add(connection.readString());
            numResponses--;
        }
        return new Response(success, responses);
    }

    public void writeToStream(Connection connection) throws IOException {
        connection.writeInt(success ? 1 : 0);
        connection.writeInt(responses.size());
        for (String response : responses) {
            connection.writeString(response);
        }
        connection.flushOutput();
    }
}
