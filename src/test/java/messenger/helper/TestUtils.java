package messenger.helper;

import messenger.objects.helper.API;
import messenger.objects.helper.APIException;
import messenger.objects.request.CreateUserRequest;
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
    public static CreateUserRequest testCreateUserRequest(String username) {
        try {
            Request request = new Request(API.CREATE_USER.getIdentifier(),
                    Arrays.asList(username));
            return new CreateUserRequest(request);
        } catch (APIException ex) {
            // This should never happen
            return null;
        }
    }
}
