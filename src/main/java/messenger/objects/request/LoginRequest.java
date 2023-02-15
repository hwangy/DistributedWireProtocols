package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

/**
 * Request object for Login API call
 */
public class LoginRequest extends SingleArgumentRequestWithUsername {
    public LoginRequest(String username) {
        super(username);
    }

    public LoginRequest(Request request) throws APIException {
        super(request);
    }

    @Override
    public int getIdentifier() {
        return API.LOGIN.getIdentifier();
    }
}
