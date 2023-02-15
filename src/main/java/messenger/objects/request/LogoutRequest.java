package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

/**
 * Request object for Logout API call
 */
public class LogoutRequest extends SingleArgumentRequestWithUsername {
    public LogoutRequest(String username) {
        super(username);
    }

    public LogoutRequest(Request request) throws APIException {
        super(request);
    }

    @Override
    public int getIdentifier() {
        return API.LOGOUT.getIdentifier();
    }
}
