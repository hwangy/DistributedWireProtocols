package messenger;

import messenger.helper.TestUtils;
import messenger.objects.response.CreateAccountResponse;
import messenger.objects.response.GetAccountsResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerCoreTest {

    @Test
    void testCreateUser() {
        ServerCore server = new ServerCore();
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));

        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().size() == 1);
    }

    @Test
    void testCreateDuplicate() {
        ServerCore server = new ServerCore();
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
        ServerCore server = new ServerCore();

        // Add an account to the server
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Assertions.assertTrue(server.getAccounts().contains(TestUtils.testUser));

        // Remove the account
        server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(server.getAccounts().contains(TestUtils.testUser));
    }

    //TODO:
    // Tests for deleting when user doesn't exist
    // Tests for GetAllAccounts with and without wild card
}
