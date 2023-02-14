package messenger;

import messenger.api.API;
import messenger.helper.TestUtils;
import messenger.network.Connection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.EOFException;
import java.io.IOException;

import static org.mockito.Mockito.when;

public class IntegrationTest {

    /**
     * Test creating a username. Mock the connection object
     * so that we can check whether users are properly connected
     * when a request is received.
     */
    @Test
    void integrationTestCreateUser() {
        // Set up client and server
        ServerCore core = new ServerCore();
        Connection connection = Mockito.mock(Connection.class);
        try {
            // First request is for choosing the method, then the
            // second is to specify number of arguments.
            // Finally, the next call to `readInt` should cause an
            // EOFException to terminate the connection.
            when(connection.readInt()).thenReturn(
                    API.CREATE_ACCOUNT.getIdentifier(),1).thenThrow(EOFException.class);
            // Then the request should specify the username.
            when(connection.readString()).thenReturn(TestUtils.testUser);
            Server.ClientHandler clientHandler = new Server.ClientHandler(connection, core);
            clientHandler.run();

            // Check user exists.
            Assertions.assertTrue(core.getAccounts().size() == 1);
            Assertions.assertTrue(core.getAccounts().contains(TestUtils.testUser));
        } catch (IOException ex) {
            Assertions.fail();
        }
    }
}
