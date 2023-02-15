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

    public SendMessageRequest(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

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

    public String getSender() {
        return this.sender;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public String getMessage() {
        return this.message;
    }
}
