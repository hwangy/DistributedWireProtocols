package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

/**
 * Request object for SendMessage API call. All messages have a
 * sender, recepient, and String message.
 */
public class SendMessageRequest implements MethodRequestInterface {
    private final String sender;
    private final String recipient;
    private final String message;

    /**
     * Specify a SendMessageRequest with the given 
     * sender, recipient, and message
     * @param sender The sender
     * @param recipient The recipient
     * @param message The message
     */
    public SendMessageRequest(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a SendMessageRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public SendMessageRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 3) {
            throw new APIException("SendMessageRequest expects 3 argument, got " + args.size());
        } else {
            this.sender = args.get(0);
            this.recipient = args.get(1);
            this.message = args.get(2);
        }
    }

    @Override
    public int getIdentifier() {
        return API.SEND_MESSAGE.getIdentifier();
    }
    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(sender, recipient, message));
    }

    /**
     * Fetch the sender associated with this request.
     * @return The sender
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * Fetch the recipient associated with this request.
     * @return The recipient
     */
    public String getRecipient() {
        return this.recipient;
    }

    /**
     * Fetch the message associated with this request.
     * @return The message
     */
    public String getMessage() {
        return this.message;
    }
}
