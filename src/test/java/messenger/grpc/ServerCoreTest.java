package messenger.grpc;

import messenger.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;


import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ServerCoreTest {

    private ServerCore server;

    private LoginReply createAndLogInUser(String user, String ipAddress) {
        server.createAccountAPI(TestUtils.testCreateUserRequest(user, ipAddress));
        return server.loginUserAPI(TestUtils.testLoginRequest(user, ipAddress));
    }

    @BeforeEach
    public void init() {
        server = new ServerCore();
    }

    /**
     * Test creating a user. User should be logged in after the fact.
     */
    @Test
    void testCreateUser() {
        createAndLogInUser(TestUtils.testUser,"");

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
        createAndLogInUser(TestUtils.testUser, "");
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
        createAndLogInUser(TestUtils.testUser, "");
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

    @Test
    /**
     * Test whether two users creating accounts on the same IP address are assigned distinct ports.
     */
    void testCreateAccountSameIPAddress() {
        // Add two users on the same IP address
        LoginReply response = createAndLogInUser(TestUtils.testUser, TestUtils.testIpAddress);
        LoginReply secondResponse = createAndLogInUser(TestUtils.testSecondUser, TestUtils.testIpAddress);

        // For two users on the same IP adress, want the message sender to assign distinct ports
        Assertions.assertTrue(response.getReceiverPort() == Constants.MESSAGE_PORT);
        Assertions.assertTrue(secondResponse.getReceiverPort() == Constants.MESSAGE_PORT + 1);

        // Delete the second user
        server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testSecondUser));
        // Recreate the second user
        secondResponse = createAndLogInUser(TestUtils.testSecondUser, TestUtils.testIpAddress);
        // Test that the second user gets port Constants.MESSAGE_PORT + 1 again
        Assertions.assertTrue(secondResponse.getReceiverPort() == Constants.MESSAGE_PORT + 1);
        // Delete the first user
        server.deleteAccountAPI(TestUtils.testDeleteUserRequest(TestUtils.testUser));
        // Recreate the first user
        response = createAndLogInUser(TestUtils.testUser, TestUtils.testIpAddress);
        // Test that the first user gets port Constants.MESSAGE_PORT again
        Assertions.assertTrue(response.getReceiverPort() == Constants.MESSAGE_PORT);

    }

    @Test
    /**
     * Test whether two users creating accounts on different IP addresses are assigned the same port.
     */
    void testCreateAccountDifferentIPAddress() {
        // Add two users on different IP address
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser, TestUtils.testIpAddress));
        LoginReply response = server.loginUserAPI(TestUtils.testLoginRequest(TestUtils.testUser, TestUtils.testIpAddress));
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testSecondUser, TestUtils.testSecondIpAddress));
        LoginReply secondResponse = server.loginUserAPI(TestUtils.testLoginRequest(TestUtils.testSecondUser, TestUtils.testSecondIpAddress));
        // For two users on the same IP adress, want the message sender to assign the same port
        Assertions.assertTrue(response.getReceiverPort() == Constants.MESSAGE_PORT);
        Assertions.assertTrue(secondResponse.getReceiverPort() == Constants.MESSAGE_PORT);
    }

    @Test
    /**
     * Testing the following functionality:
     * 1. User 1 creates account
     * 2. User 2 creates account on same ip
     * 3. User 1 logs out
     * 4. User 3 creates account on same ip
     * 5. User 4 creates account on same ip
     * Here, user 3 should get the same port number as user 1 initially did
     * User 4 should get the port Constants.MESSAGE_PORT + 2
     */
    void testCreateThreeAccountsIPAddress () {
        // Login two users on same IP address
        LoginReply response = createAndLogInUser(TestUtils.testUser, TestUtils.testIpAddress);
        LoginReply secondResponse = createAndLogInUser(TestUtils.testSecondUser, TestUtils.testIpAddress);
        Assertions.assertTrue(response.getReceiverPort() == Constants.MESSAGE_PORT);
        Assertions.assertTrue(secondResponse.getReceiverPort() == Constants.MESSAGE_PORT + 1);

        // Logout first user
        server.logoutUserAPI(TestUtils.testLogoutTestUser(TestUtils.testUser));

        // User 3 creates account on same IP
        LoginReply thirdResponse = createAndLogInUser(TestUtils.testThirdUser, TestUtils.testIpAddress);

        // User 3 should get the same port number as user 1 initially did
        Assertions.assertTrue(thirdResponse.getReceiverPort() == Constants.MESSAGE_PORT);

        // User 4 creates account on same IP
        LoginReply fourthResponse = createAndLogInUser(TestUtils.testFourthUser, TestUtils.testIpAddress);

        // User 4 should get port Constants.MESSAGE_PORT + 2
        Assertions.assertTrue(fourthResponse.getReceiverPort() == Constants.MESSAGE_PORT + 2);
    }

    /**
     * Ensure that undelivered messages are only delivered once.
     */
    @Test
    void testGetUndeliveredMessages () {
        server.createAccountAPI(TestUtils.testCreateUserRequest(TestUtils.testUser, TestUtils.testIpAddress));
        // Log out the first user
        server.logoutUserAPI(TestUtils.testLogoutTestUser());
        // This message should be undelivered
        server.sendMessageAPI(TestUtils.testSendToTestUser());
        GetUndeliveredMessagesReply reply =
                server.getUndeliveredMessagesAPI(TestUtils.testGetUndeliveredMessagesToTestUser());
        Assertions.assertEquals(1, reply.getMessagesList().size());

        // Now that we've gotten the messages, the length should be 0
        reply = server.getUndeliveredMessagesAPI(TestUtils.testGetUndeliveredMessagesToTestUser());
        Assertions.assertEquals(0, reply.getMessagesList().size());
    }

    /**
     * Testing the function for adding a user to the server's set of accounts and to the all_users.txt file
     */
    @Test 
    void testPersistenceAddUser() {
        try{
            server.addUser(TestUtils.testUser);
            BufferedReader usersReader = new BufferedReader(new FileReader(Constants.getUsersFileName(0)));
            String usersList = usersReader.readLine();
            Gson gson = new Gson();
            HashSet<String> usersSet = gson.fromJson(usersList, new TypeToken<HashSet<String>>(){}.getType());
            Assertions.assertTrue(server.getAccounts().equals(usersSet));
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Testing the function for deleting a user from the server's set of accounts and to the all_users.txt file
     */
    @Test 
    void testPersistenceDeleteUser() {
        try{
            server.deleteUser(TestUtils.testUser);
            BufferedReader usersReader = new BufferedReader(new FileReader(Constants.getUsersFileName(0)));
            String usersList = usersReader.readLine();
            Gson gson = new Gson();
            HashSet<String> usersSet = gson.fromJson(usersList, new TypeToken<HashSet<String>>(){}.getType());
            Assertions.assertTrue(server.getAccounts().equals(usersSet));
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Testing the function for removing an undelivered messages from the server's map of undelivered messages and from the undelivered_messages.txt file
     */
    @Test 
    void testPersistenceRemoveUndeliveredMessage () {
        try{
            server.removeUndeliveredMessage(TestUtils.testUser);
            BufferedReader undeliveredMessagesReader = new BufferedReader(new FileReader(Constants.getUndeliveredFileName(0)));
            String undeliveredMsgs = undeliveredMessagesReader.readLine();
            Gson gson = new Gson();
            HashMap<String, List<Message>> undeliveredMsgsFromJSON = gson.fromJson(undeliveredMsgs, new TypeToken<HashMap<String, List<Message>>>(){}.getType());
            Assertions.assertTrue(server.getUndeliveredMessagesMap().equals(undeliveredMsgsFromJSON));
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Testing the function for adding an undelivered messages to the server's map of undelivered messages and to the undelivered_messages.txt file
     */
    @Test
    void testPersistenceAddUndeliveredMessage () {
        try{
            Message message = Message.newBuilder().setSender(TestUtils.testUser).setRecipient(TestUtils.testSecondUser).setMessage(TestUtils.testMessage).build();
            server.addUndeliveredMessage(message);
            BufferedReader undeliveredMessagesReader = new BufferedReader(new FileReader(Constants.getUndeliveredFileName(0)));
            String undeliveredMsgs = undeliveredMessagesReader.readLine();
            Gson gson = new Gson();
            HashMap<String, List<Message>> undeliveredMsgsFromJSON = gson.fromJson(undeliveredMsgs, new TypeToken<HashMap<String, List<Message>>>(){}.getType());
            Assertions.assertTrue(server.getUndeliveredMessagesMap().equals(undeliveredMsgsFromJSON));
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }
}
