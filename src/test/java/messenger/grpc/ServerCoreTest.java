package messenger.grpc;

import messenger.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class ServerCoreTest {

    private ServerCore server;

    @BeforeEach
    public void init() {
        server = new ServerCore();
    }

    /**
     * Test creating a user. User should be logged in after the fact.
     */
    @Test
    void testCreateUser() {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));

        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().size() == 1);

        Assertions.assertTrue(server.isLoggedIn(TestUtils.testUser));
    }

    /**
     * Test creating duplicate user. The second attempt should fail.
     */
    @Test
    void testCreateDuplicate() {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        LoginReply response = server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));

        // The second attempt to create a user should fail
        Assertions.assertFalse(response.getStatus().getSuccess());

        // But the first user should still be contained in the list
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().size() == 1);
    }

    /**
     * Test deleting a user. User should be logged out after the fact.
     */
    @Test
    void testDeleteUser() {
        // Add an account to the server
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));

        // Remove the account
        server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(server.getAccounts().contains(TestUtils.testUser));

        // User should be logged out.
        Assertions.assertFalse(server.isLoggedIn(TestUtils.testUser));
    }

    /**
     * Test whether deleting a non-existent user fails.
     */
    @Test
    void testDeletingNonExistentUser() {
        StatusReply response = server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(response.getStatus().getSuccess());
    }

    /**
     * Tests whether called getAccounts WITHOUT a wildcard
     * returns a list of all users, as expected.
     */
    @Test
    void testGetAccountsNoWildcard() {
        // Add two users.
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testSecondUser));

        GetAccountsReply response = server.getAccountsAPI(TestUtils.testGetAllAccountsRequest());
        Assertions.assertEquals(2, response.getAccountsList().size());
    }

    /**
     * Tests whether calling getAccounts with a regex wildcard
     * properly selects matching accounts only.
     */
    @Test
    void testGetAccountsWithWildcard() {
        // Add two users.
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testSecondUser));
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.uniquePrefixUser));

        GetAccountsReply response = server.getAccountsAPI(TestUtils.testGetAccountsMatchUniquePrefix());
        Assertions.assertEquals(1, response.getAccountsList().size());
    }

    /**
     * Tests sending a message to a not-logged-in user. Such a message
     * should be added to `undeliveredMessages`.
     */
    @Test
    void testSendMessageNotLoggedIn() {
        // Create and log out the user
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        server.logoutUserAPI(TestUtils.testLogoutTestUser());

        Assertions.assertTrue(server.sendMessageAPI(TestUtils.testSendToTestUser())
                .getStatus().getSuccess());

        GetUndeliveredMessagesReply response = server.getUndeliveredMessagesAPI(
                TestUtils.testGetUndeliveredMessagesToTestUser());
        Assertions.assertTrue(response.getStatus().getSuccess());
        List<Message> messages = response.getMessagesList();
        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals(TestUtils.testMessage, messages.get(0).getMessage());
    }

    /**
     * Test sending a message to a logged in user.
     */
    @Test
    void testSendMessageLoggedIn() {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        server.sendMessageAPI(TestUtils.testSendToTestUser());

        Optional<List<Message>> messageList = server.getQueuedMessages(TestUtils.testUser);
        Assertions.assertTrue(messageList.isPresent());
        Assertions.assertEquals(TestUtils.testMessage, messageList.get().get(0).getMessage());
    }

    /**
     * Test whether logging a user out correctly removes them from
     * the loggedIn users set.
     */
    @Test
    void testLogoutUser() {
        // User should start logged off.
        Assertions.assertFalse(server.isLoggedIn(TestUtils.testUser));
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        // And now they should be logged in.
        Assertions.assertTrue(server.isLoggedIn(TestUtils.testUser));

        // Logout the user and ensure they are correctly logged out.
        server.logoutUserAPI(TestUtils.testLogoutTestUser());
        Assertions.assertFalse(server.isLoggedIn(TestUtils.testUser));

        // Account should still exist
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
    }

    /**
     * Test whether trying to log in a user which has not been created fails.
     */
    @Test
    void testLoginNotCreateUser() {
        LoginReply response = server.loginUserAPI(TestUtils.testLoginRequest(TestUtils.testUser));
        Assertions.assertFalse(response.getStatus().getSuccess());
    }
}
