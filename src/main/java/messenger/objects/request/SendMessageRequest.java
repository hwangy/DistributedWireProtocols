package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

public class SendMessageRequest implements MethodRequestInterface {
    private final String recipient;
    private final String message;

    public SendMessageRequest(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    public SendMessageRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 2) {
            throw new APIException("SendMessageRequest expects 2 argument, got " + args.size());
        } else {
            this.recipient = args.get(0);
            this.message = args.get(1);
        }
    }

    @Override
    public int getIdentifier() {
        return API.SEND_MESSAGE.getIdentifier();
    }
    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(new String[]{recipient}));
    }

    public String getRecipient() {
        return this.recipient;
    }

    public String getMessage() {
        return this.message;
    }
}
