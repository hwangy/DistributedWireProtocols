package messenger;

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

        client.loginAPI(TestUtils.testLoginRequest(TestUtils.testUser),
                TestUtils.testSuccessfulStatusMessageResponse());
        // Is logged in after login request is executed.
        Assertions.assertTrue(client.isLoggedIn());
    }

    @Test
    void testLogout() {
        ClientCore client = new ClientCore();
        client.loginAPI(TestUtils.testLoginRequest(TestUtils.testUser),
                TestUtils.testSuccessfulStatusMessageResponse());
        // Should be logged in
        Assertions.assertTrue(client.isLoggedIn());

        client.logoutAPI(TestUtils.testSuccessfulStatusMessageResponse());
        Assertions.assertFalse(client.isLoggedIn());
    }

}
