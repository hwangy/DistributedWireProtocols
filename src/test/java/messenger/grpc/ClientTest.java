package messenger.grpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests client-specific behavior. The primary responsibility of the
 * client is to maintain a user's logged in state correctly. Thus,
 * this is the focus of these tests.
 */
class ClientTest {

    /**
     * Tests whether the client's `loginAPI` correctly logs in a user.
     */
    @Test
    void testLogin() {
        ClientCore client = new ClientCore();
        // Starts off not logged in.
        Assertions.assertFalse(client.isLoggedIn());

        client.setLoggedInStatus(TestUtils.testUser, TestUtils.testSuccessfulLoginReply());
        // Is logged in after login request is executed.
        Assertions.assertTrue(client.isLoggedIn());
    }

    @Test
    void testLogout() {
        ClientCore client = new ClientCore();
        client.setLoggedInStatus(TestUtils.testUser, TestUtils.testSuccessfulLoginReply());
        // Should be logged in
        Assertions.assertTrue(client.isLoggedIn());

        client.setLoggedOutStatus(TestUtils.testSuccessfulStatusMessageResponse());
        Assertions.assertFalse(client.isLoggedIn());
    }

}
