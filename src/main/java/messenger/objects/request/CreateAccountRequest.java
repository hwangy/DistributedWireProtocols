package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

/**
 * Request object for CreateAccount API call
 */
public class CreateAccountRequest extends SingleArgumentRequestWithUsername {
    
    /**
     * Specify a CreateAccountRequest with the given 
     * username.
     * @param username The username.
     */
    public CreateAccountRequest(String username) {
        super(username);
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a CreateAccountRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
    public CreateAccountRequest(Request request) throws APIException {
        super(request);
    }

    @Override
    public int getIdentifier() {
        return API.CREATE_ACCOUNT.getIdentifier();
    }
}
