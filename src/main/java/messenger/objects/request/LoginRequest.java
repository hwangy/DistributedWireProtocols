package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

/**
 * Request object for Login API call
 */
public class LoginRequest extends SingleArgumentRequestWithUsername {
    
    /**
     * Specify a LoginRequest with the given 
     * username
     * @param username The username
     */
    public LoginRequest(String username) {
        super(username);
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a LoginRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public LoginRequest(Request request) throws APIException {
        super(request);
    }

    @Override
    public int getIdentifier() {
        return API.LOGIN.getIdentifier();
    }
}
