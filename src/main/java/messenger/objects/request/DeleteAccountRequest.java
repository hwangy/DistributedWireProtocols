package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;


/**
 * Request object for DeleteAccount API call
 */
public class DeleteAccountRequest extends SingleArgumentRequestWithUsername {

    /**
     * Specify a DeleteAccountRequest with the given 
     * username
     * @param username The username
     */
    public DeleteAccountRequest(String username) {
        super(username);
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a DeleteAccountRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public DeleteAccountRequest(Request request) throws APIException {
        super(request);
    }
    
    /**
     * Gets the identifier associated to Delete Account 
     * @return The identifier
     */
    @Override
    public int getIdentifier() {
        return API.DELETE_ACCOUNT.getIdentifier();
    }
}
