package messenger.helper;

import messenger.api.API;
import messenger.api.APIException;
import messenger.objects.request.*;
import messenger.objects.response.StatusMessageResponse;

import java.util.Arrays;

/**
 * Basic test utilities
 */
public class TestUtils {
    public static final String testUser = "testUser";
    public static final String testSecondUser = "testUser2";
    public static final String uniquePrefixUser = "userTest";
    public static final String matchingPrefix = "user.*";

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

    /**
     * Create a simple LoginRequest.
     * @param username  Username to log in.
     * @return          a LoginRequest
     */
    public static LoginRequest testLoginRequest(String username) {
        try {
            Request request = new Request(API.LOGIN.getIdentifier(), Arrays.asList(username));
            return new LoginRequest(request);
        } catch (APIException ex) {
            // This should never happen
            return null;
        }
    }

    /**
     * A test StatusMessageResponse indicating a successful exection
     * of an API call.
     * @return  A StatusMessageResponse object
     */
    public static StatusMessageResponse testSuccessfulStatusMessageResponse() {
        return new StatusMessageResponse(true, "success");
    }

    public static GetAccountsRequest testGetAllAccountsRequest() {
        return new GetAccountsRequest();
    }

    /**
     * Creates a test GetAccountsRequest which only matches the test user
     * with a unique prefix.
     * @return  A GetAccountsRequest with wildcard "user.*"
     */
    public static GetAccountsRequest testGetAccountsMatchUniquePrefix() {
        return new GetAccountsRequest(matchingPrefix);
    }
}
