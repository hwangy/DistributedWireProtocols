package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

public class DeleteAccountRequest extends SingleArgumentRequestWithUsername {

    public DeleteAccountRequest(String username) {
        super(username);
    }

    public DeleteAccountRequest(Request request) throws APIException {
        super(request);
    }
    @Override
    public int getIdentifier() {
        return API.DELETE_ACCOUNT.getIdentifier();
    }
}
