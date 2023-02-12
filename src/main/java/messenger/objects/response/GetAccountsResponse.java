package messenger.objects.response;

import java.util.List;

/**
 * A response from a request to get all accounts.
 */
public class GetAccountsResponse extends ListMessageResponse {
   public GetAccountsResponse(Boolean success, List<String> accounts) {
       super(success, accounts);
   }
}
