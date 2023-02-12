package messenger;

import org.junit.jupiter.api.Test;

public class IntegrationTest {
    @Test
    void integrationTestOne() {
        // Set up client and server
        ServerCore core = new ServerCore();
        Server.ClientHandler clientHandler = new Server.ClientHandler(null, core);
    }
}
