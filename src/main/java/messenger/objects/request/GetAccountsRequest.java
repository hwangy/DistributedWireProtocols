package messenger.objects.request;

import messenger.api.API;
import messenger.api.APIException;

import java.util.Arrays;
import java.util.List;

/**
 * Request object for GetAccounts API call
 */
public class GetAccountsRequest implements MethodRequestInterface {
    private final String textWildcard;

    /**
     * The default constructor which sets an empty
     * wildcard; such a request will be interpretted
     * as asking for ALL accounts.
     */
    public GetAccountsRequest() {
        this.textWildcard = "";
    }

    /**
     * Specify a GetAccountRequest with the given
     * regex wildcard.
     * @param textWildcard  The regex wildcard.
     */
    public GetAccountsRequest(String textWildcard) {
        this.textWildcard = textWildcard;
    }

    /**
     * Converts a generic Request (e.g. received by
     * the server) into a GetAccountRequest.
     * @param request       The generic request.
     * @throws APIException Thrown on API-level exception.
     */
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

    /**
     * Fetch the wildcard associated with this request.
     * @return  The regex wildcard.
     */
    public String getTextWildcard() {
        return this.textWildcard;
    }
}
