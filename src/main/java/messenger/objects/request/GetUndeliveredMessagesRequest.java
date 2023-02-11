package messenger.objects.request;

import messenger.objects.helper.API;
import messenger.objects.helper.APIException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetUndeliveredMessagesRequest implements MethodRequestInterface {
    private final String username;

    public GetUndeliveredMessagesRequest(String username) {
        this.username = username;
    }

    public GetUndeliveredMessagesRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 1) {
            throw new APIException("GetUndeliveredMessagesRequest expects 1 argument, got " + args.size());
        } else {
            this.username = args.get(0);
        }
    }

    @Override
    public int getIdentifier() {
        return API.GET_UNDELIVERED_MESSAGES.getIdentifier();
    }
    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(new String[]{username}));
    }

    public String getUsername() {
        return this.username;
    }
}
