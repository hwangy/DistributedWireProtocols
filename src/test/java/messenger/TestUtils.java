package messenger;

import messenger.grpc.*;
import messenger.util.Constants;

/**
 * Basic test utilities
 */
public class TestUtils {
    public static final String testUser = "testUser";
    public static final String testSecondUser = "testUser2";
    public static final String uniquePrefixUser = "userTest";
    public static final String matchingPrefix = "user.*";
    public static final String testMessage = "test message";
    public static final String testIpAddress = "127.0.0.1";
    public static final String testSecondIpAddress = "127.0.0.2";
    public static final int testPort = Constants.MESSAGE_PORT;

    /**
     * Creates a simple CreateAccountRequest
     * @param username  Username for the request
     * @return          a createAccountRequest
     */
    public static CreateAccountRequest testCreateUserRequest(String username) {
        return CreateAccountRequest.newBuilder()
                .setIpAddress(testIpAddress)
                .setUsername(username).build();
    }

    /**
     * Creates a simple CreateAccountRequest
     * @param username  Username for the request
     * @param ipAddress IP address for the request
     * @return          a createAccountRequest
     */
    public static CreateAccountRequest testCreateUserRequest(String username, String ipAddress) {
        return CreateAccountRequest.newBuilder()
                .setIpAddress(ipAddress)
                .setUsername(username).build();
    }

    /**
     * Create a simple DeleteUserRequest
     * @param username  Username for the request
     * @return          a deleteUserRequest
     */
    public static DeleteAccountRequest testDeleteUserRequest(String username) {
        return DeleteAccountRequest.newBuilder()
                .setUsername(username)
                .build();
    }

    /**
     * Create a simple LoginRequest.
     * @param username  Username to log in.
     * @param ipAddress IP address for the request
     * @return          a LoginRequest
     */
    public static LoginRequest testLoginRequest(String username, String ipAddress) {
        return LoginRequest.newBuilder().setIpAddress(ipAddress).setUsername(username).build();
    }

    /**
     * Create a simple LoginRequest.
     * @param username  Username to log in.
     * @return          a LoginRequest
     */
    public static LoginRequest testLoginRequest(String username) {
        return LoginRequest.newBuilder().setIpAddress(testIpAddress).setUsername(username).build();
    }

    private static Status testSuccessfulStatus() {
        return Status.newBuilder().setSuccess(true).build();
    }

    /**
     * A test StatusMessageResponse indicating a successful exection
     * of an API call.
     * @return  A StatusMessageResponse object
     */
    public static StatusReply testSuccessfulStatusMessageResponse() {
        return StatusReply.newBuilder().setStatus(testSuccessfulStatus()).build();
    }

    public static LoginReply testSuccessfulLoginReply() {
        return LoginReply.newBuilder().setReceiverPort(testPort).setStatus(testSuccessfulStatus())
                .build();
    }

    public static GetAccountsRequest testGetAllAccountsRequest() {
        return GetAccountsRequest.newBuilder().setTextWildcard("").build();
    }

    /**
     * Creates a test GetAccountsRequest which only matches the test user
     * with a unique prefix.
     * @return  A GetAccountsRequest with wildcard "user.*"
     */
    public static GetAccountsRequest testGetAccountsMatchUniquePrefix() {
        return GetAccountsRequest.newBuilder().setTextWildcard(matchingPrefix).build();
    }

    /**
     * Create a SendMessageRequest to testUser containg the test message.
     * @return  A SendMessageRequest.
     */
    public static SendMessageRequest testSendToTestUser() {
        return SendMessageRequest.newBuilder().setMessage(Message.newBuilder()
                .setSender("")
                .setRecipient(testUser)
                .setMessage(testMessage).build()).build();
    }

    /**
     * Create a test request for undelivered messages to the `testUser`.
     * @return  A GetUndeliveredMessagesRequest
     */
    public static GetUndeliveredMessagesRequest testGetUndeliveredMessagesToTestUser() {
        return GetUndeliveredMessagesRequest.newBuilder()
                .setUsername(testUser)
                .build();
    }

    /**
     * A test request for logging out `testUser`.
     * @return  A LogoutRequest
     */
    public static LogoutRequest testLogoutTestUser() {
        return LogoutRequest.newBuilder().setUsername(testUser).build();
    }
}
