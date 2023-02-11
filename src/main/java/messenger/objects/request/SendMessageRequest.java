package messenger.objects.request;

import messenger.objects.helper.API;
import messenger.objects.helper.APIException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendMessageRequest implements MethodRequestInterface {
    private final String recipient;

    public SendMessageRequest(String recipient) {
        this.recipient = recipient;
    }

    public SendMessageRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 1) {
            throw new APIException("SendMessageRequest expects 1 argument, got " + args.size());
        } else {
            this.recipient = args.get(0);
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
}
