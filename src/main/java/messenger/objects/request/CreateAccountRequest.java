package messenger.objects.request;

import messenger.objects.helper.API;
import messenger.objects.helper.APIException;

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
