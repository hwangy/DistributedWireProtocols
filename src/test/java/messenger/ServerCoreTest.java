package messenger;

import messenger.helper.TestUtils;
import messenger.objects.response.CreateAccountResponse;
import messenger.objects.response.DeleteUserResponse;
import messenger.objects.response.GetAccountsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerCoreTest {

    private ServerCore server;

    @BeforeEach
    public void init() {
        server = new ServerCore();
    }

    @Test
    void testCreateUser() {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));

        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().size() == 1);
    }

    @Test
    void testCreateDuplicate() {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        CreateAccountResponse response = server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));

        // The second attempt to create a user should fail
        Assertions.assertFalse(response.isSuccessful());

        // But the first user should still be contained in the list
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().size() == 1);
    }

    @Test
    void testDeleteUser() {
        // Add an account to the server
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));

        // Remove the account
        server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(server.getAccounts().contains(TestUtils.testUser));
    }

    @Test
    void testDeletingNonExistentUser() {
        DeleteUserResponse response = server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(response.isSuccessful());
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

        GetAccountsResponse response = server.getAccountsAPI(TestUtils.testGetAllAccountsRequest());
        Assertions.assertEquals(2, response.getMessages().size());
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

        GetAccountsResponse response = server.getAccountsAPI(TestUtils.testGetAccountsMatchUniquePrefix());
        Assertions.assertEquals(1, response.getMessages().size());
    }
}
