package messenger;

import messenger.helper.TestUtils;
import messenger.objects.Status;
import messenger.objects.helper.APIException;
import messenger.objects.request.CreateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServerCoreTest {

    @Test
    void testCreateUser() {
        ServerCore server = new ServerCore();
        server.createUser(TestUtils.testCreateUserRequest(TestUtils.testUser));

        Assertions.assertTrue(server.getAllUsers().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAllUsers().size() == 1);
    }

    @Test
    void testCreateDuplicate() {
        ServerCore server = new ServerCore();
        server.createUser(TestUtils.testCreateUserRequest(TestUtils.testUser));
        Status status = server.createUser(TestUtils.testCreateUserRequest(TestUtils.testUser));

        // The second attempt to create a user should fail
        Assertions.assertFalse(status.isSuccess());

        // But the first user should still be contained in the list
        Assertions.assertTrue(server.getAllUsers().contains(TestUtils.testUser));
        Assertions.assertTrue(server.getAllUsers().size() == 1);
    }
}
