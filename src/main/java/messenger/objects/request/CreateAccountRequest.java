package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

public class CreateAccountRequest extends SingleArgumentRequestWithUsername {
    public CreateAccountRequest(String username) {
        super(username);
    }

    public CreateAccountRequest(Request request) throws APIException {
        super(request);
    }

    @Override
    public int getIdentifier() {
        return API.CREATE_ACCOUNT.getIdentifier();
    }
}
