package messenger.objects.response;

import messenger.network.Connection;
import messenger.util.Logging;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Response {
    private final Boolean success;
    private final List<String> responses;

    /**
     * Form a Response object with the given
     * success and responses parameters.
     * @param success Indicates if there was a success
     * @param message The responses
     */
    public Response(Boolean success, List<String> responses) {
        this.success = success;
        this.responses = responses;
    }

    /**
     * Get the indicator of success of the Response
     */
    public Boolean isSuccessful() {
        return success;
    }

    /**
     * Get the responses of the Response
     */
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

    /**
     * Write to stream corresponding to the connection object
     * @input connection    A connection object to write to
     */
    public void writeToStream(Connection connection) throws IOException {
        connection.writeInt(success ? 1 : 0);
        connection.writeInt(responses.size());
        for (String response : responses) {
            connection.writeString(response);
        }
        connection.flushOutput();
    }

    /**
     * Print the responses
     */
    public void printResponses() {
        for (String response : responses) {
            Logging.logService(response);
        }
    }
}
