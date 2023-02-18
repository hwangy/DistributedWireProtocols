package messenger.objects.response;

import messenger.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Response object for GetUndeliveredMessages API call
 */
public class GetUndeliveredMessagesResponse extends ListMessageResponse<Message> {
    
    /**
     * Specify a GetUndeliveredMessagesResponse with the given
     * success and messages parameters.
     * @param success Indicates if there was a success
     * @param messages The messages
     */
    public GetUndeliveredMessagesResponse(Boolean success, List<Message> messages) {
        super(success, messages);
    }

    /**
     * Creates a generic response associated with the response
     * @return The generic response associated with the response
     */
    @Override
    public Response genGenericResponse() {
        List<String> combinedList = new ArrayList<>();
        for (Message message : getMessages()) {
            combinedList.addAll(message.asStringList());
        }
        return new Response(isSuccessful(), combinedList);
    }

    /**
     * Get the status string
     * @return The status string
     */
    @Override
    public String getStringStatus() {
        if (isSuccessful()) {
            return "Undelivered message request returned " + getMessages().size() + " messages.";
        } else {
            return "Failed to fetch undelivered messages.";
        }
    }
}
