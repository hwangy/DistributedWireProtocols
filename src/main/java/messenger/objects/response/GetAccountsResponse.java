package messenger.objects.response;

import java.util.List;

/**
 * A response from a request to get all accounts. Response object for CreateAccount API call.
 */
public class GetAccountsResponse extends ListMessageResponse {

    /**
     * Specify a GetAccountsResponse with the given
     * success and accounts parameters.
     * @param success Indicates if there was a success
     * @param accounts The accounts
     */
   public GetAccountsResponse(Boolean success, List<String> accounts) {
       super(success, accounts);
   }
}
