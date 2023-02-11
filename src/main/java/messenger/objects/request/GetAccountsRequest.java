package messenger.objects.request;

import messenger.objects.helper.API;
import messenger.objects.helper.APIException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetAccountsRequest implements MethodRequestInterface {
    private final String text_wildcard;

    public GetAccountsRequest(String text_wildcard) {
        this.text_wildcard = text_wildcard;
    }

    public GetAccountsRequest(Request request) throws APIException {
        List<String> args = request.getArguments();
        if (args.size() != 1) {
            throw new APIException("GetAccountsRequest expects 1 argument, got " + args.size());
        } else {
            this.text_wildcard = args.get(0);
        }
    }

    @Override
    public int getIdentifier() {
        return API.GET_ACCOUNTS.getIdentifier();
    }
    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(new String[]{text_wildcard}));
    }

    public String getTextWildcard() {
        return this.text_wildcard;
    }
}
