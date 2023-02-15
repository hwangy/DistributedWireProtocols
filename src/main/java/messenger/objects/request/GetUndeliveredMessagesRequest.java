package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

/**
 * Request object for GetUndeliveredMessages API call
 */
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

    /**
     * Gets the username of the receiver of the
     * the requested messages.
     * @return  The username.
     */
    public String getUsername() {
        return this.username;
    }
}
