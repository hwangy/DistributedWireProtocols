package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

public class GetAccountsRequest implements MethodRequestInterface {
    private final String textWildcard;

    public GetAccountsRequest() {
        this.textWildcard = "";
    }

    public GetAccountsRequest(String textWildcard) {
        this.textWildcard = textWildcard;
    }

    public GetAccountsRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 1) {
            throw new APIException("GetAccountsRequest expects 1 argument, got " + args.size());
        } else {
            this.textWildcard = args.get(0);
        }
    }

    @Override
    public int getIdentifier() {
        return API.GET_ACCOUNTS.getIdentifier();
    }

    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(textWildcard));
    }

    public String getTextWildcard() {
        return this.textWildcard;
    }
}
