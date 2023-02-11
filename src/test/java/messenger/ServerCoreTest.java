package messenger;

import messenger.helper.TestUtils;
import messenger.objects.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerCoreTest {

    @Test
    void testCreateUser() {
        ServerCore server = new ServerCore();
        server.createAccount(TestUtils.testCreateUserRequest(TestUtils.testUser));

        Assertions.assertTrue(server.getAllAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAllAccounts().size() == 1);
    }

    @Test
    void testCreateDuplicate() {
        ServerCore server = new ServerCore();
        server.createAccount(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Status status = server.createAccount(TestUtils.testCreateUserRequest(TestUtils.testUser));

        // The second attempt to create a user should fail
        Assertions.assertFalse(status.isSuccess());

        // But the first user should still be contained in the list
        Assertions.assertTrue(server.getAllAccounts().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAllAccounts().size() == 1);
    }

    @Test
    void testDeleteUser() {
        ServerCore server = new ServerCore();

        // Add an account to the server
        server.createAccount(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Assertions.assertTrue(server.getAllAccounts().contains(TestUtils.testUser));

        // Remove the account
        server.deleteAccount(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        Assertions.assertFalse(server.getAllAccounts().contains(TestUtils.testUser));
    }
}
