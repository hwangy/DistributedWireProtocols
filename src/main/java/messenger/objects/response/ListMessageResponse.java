package messenger.objects.response;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Response object for ListMessageResponse API call
 */
public class ListMessageResponse<T extends Object> implements MethodResponseInterface {
    private final Boolean success;
    private final List<T> message;

    /**
     * Specify a ListMessageResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public ListMessageResponse(Boolean success, List<T> message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Fetch the message associated with this response.
     * @return The message
     */
    public List<T> getMessages() {
        return message;
    }

    /**
     * Fetch the success indicator associated with this response.
     * @return The success indicator
     */
    public Boolean isSuccessful() {
        return success;
    }
    
    /**
     * Get the status string
     * @return The status string
     */
    @Override
    public String getStringStatus() {
        String response;
        if (success) {
            response = "Request processed successfully";
            if (message.size() > 0) {
                response += " with first message \"" + message.get(0) + "\"";
            }
        } else {
            response = "Request failed";
            if (message.size() > 0) {
                response += " with message \"" + message.get(0) + "\"";
            }
        }
        return response;
    }

    /**
     * Creates a generic response assocated with the response
     * @return The generic response assocated with the response
     */
    @Override
    public Response genGenericResponse() {
        return new Response(success, message.stream().map(
                Object::toString).collect(Collectors.toList()));
    }
}
