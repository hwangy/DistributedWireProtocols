package messenger.objects.request;

import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

public abstract class SingleArgumentRequestWithUsername implements MethodRequestInterface {
    private final String username;

    public SingleArgumentRequestWithUsername(String username) {
        this.username = username;
    }

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

    public String getUsername() {
        return this.username;
    }
}
