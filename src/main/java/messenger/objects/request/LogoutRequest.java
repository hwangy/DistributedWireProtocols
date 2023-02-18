package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

/**
 * Request object for Logout API call
 */
public class LogoutRequest extends SingleArgumentRequestWithUsername {
    
    /**
     * Specify a LogoutRequest with the given 
     * username
     * @param username The username
     */
    public LogoutRequest(String username) {
        super(username);
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a LogoutRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public LogoutRequest(Request request) throws APIException {
        super(request);
    }

    /**
     * Gets the identifier associated to Logout
     * @return The identifier
     */
    @Override
    public int getIdentifier() {
        return API.LOGOUT.getIdentifier();
    }
}
