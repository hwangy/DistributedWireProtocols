package messenger.objects.request;

import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract class which represents a request with only
 * a single argument: the username.
 */
public abstract class SingleArgumentRequestWithUsername implements MethodRequestInterface {
    private final String username;

    /**
     * Specify a SingleArgumentRequestWithUsername with the given 
     * username.
     * @param username The username.
     */
    public SingleArgumentRequestWithUsername(String username) {
        this.username = username;
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a SingleArgumentRequestWithUsername.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public SingleArgumentRequestWithUsername(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 1) {
            throw new APIException("Expected 1 argument (username), got " + args.size());
        } else {
            this.username = args.get(0);
        }
    }

    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(username));
    }

    /**
     * Fetch the username associated with this request.
     * @return The username.
     */
    public String getUsername() {
        return this.username;
    }
}
