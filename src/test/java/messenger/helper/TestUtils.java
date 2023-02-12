package messenger.helper;

import messenger.api.API;
import messenger.api.APIException;
import messenger.objects.request.CreateAccountRequest;
import messenger.objects.request.DeleteAccountRequest;
import messenger.objects.request.GetAccountsRequest;
import messenger.objects.request.Request;

import java.util.Arrays;

/**
 * Basic test utilities
 */
public class TestUtils {
    public static final String testUser = "testUser";
    public static final String testSecondUser = "testUser2";
    /**
     * Creates a simple CreateUserRequest
     * @param username  Username for the request
     * @return          a createUserRequest
     */
    public static CreateAccountRequest testCreateUserRequest(String username) {
        try {
            Request request = new Request(API.CREATE_ACCOUNT.getIdentifier(),
                    Arrays.asList(username));
            return new CreateAccountRequest(request);
        } catch (APIException ex) {
            // This should never happen
            return null;
        }
    }

    /**
     * Create a simple DeleteUserRequest
     * @param username  Username for the request
     * @return          a deleteUserRequest
     */
    public static DeleteAccountRequest testDeleteUserRequest(String username) {
        try {
            Request request = new Request(API.DELETE_ACCOUNT.getIdentifier(),
                    Arrays.asList(username));
            return new DeleteAccountRequest(request);
        } catch (APIException ex) {
            // This should never happen
            return null;
        }
    }

    public static GetAccountsRequest testGetAllAccountsRequest() {
        return new GetAccountsRequest();
    }
}
