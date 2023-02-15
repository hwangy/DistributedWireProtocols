package messenger;

import messenger.objects.Message;
import messenger.objects.request.*;
import messenger.objects.response.*;

import java.util.*;

public class ServerCore {
    private final Map<String, List<Message>> sentMessages;
    private final Map<String, List<Message>> queuedMessagesMap;

    private final Map<String, List<Message>> undeliveredMessages;
    private final Set<String> loggedInUsers;
    private final Set<String> allAccounts;

    public ServerCore() {
       this.sentMessages = new HashMap<>();
       this.queuedMessagesMap = new HashMap<>();
       this.undeliveredMessages = new HashMap<>();
       this.loggedInUsers = new HashSet<>();
       this.allAccounts = new HashSet<>();
    }

    public Set<String> getAccounts() {
        return allAccounts;
    }

    /**
     * Queue message for delivery (whether immediately or later
     * if the user is not logged in).
     *
     * @param message   Message to be queued.
     */
    private void addMessageToList(Map<String, List<Message>> listToAdd, Message message) {
        List<Message> messageList;
        String recepient = message.getRecepient();
        if (listToAdd.containsKey(recepient)) {
            messageList = listToAdd.get(recepient);
        } else {
            messageList = new ArrayList<>();
            listToAdd.put(recepient, messageList);
        }
        messageList.add(message);
    }

    public Optional<List<Message>> getQueuedMessages(String username) {
        if (queuedMessagesMap.containsKey(username)) {
            return Optional.of(queuedMessagesMap.get(username));
        } else {
            return Optional.empty();
        }
    }

    public void unqueueMessages(String username) {
        List<Message> messageList;

        if (sentMessages.containsKey(username)) {
            messageList = sentMessages.get(username);
        } else {
            messageList = new ArrayList<Message>();
            sentMessages.put(username, messageList);
        }

        Optional<List<Message>> queuedMessages = getQueuedMessages(username);
        if (queuedMessages.isPresent()) {
            messageList.addAll(queuedMessages.get());
            queuedMessagesMap.remove(username);
        }
    }

    public Boolean isLoggedIn(String username) {
        return loggedInUsers.contains(username);
    }

    public void logoutUser(String username) {
        loggedInUsers.remove(username);
    }

    /**
     * Fetches all users registered with the server.
     *
     * @return  a list of all users.
     */
    public GetAccountsResponse getAccountsAPI(GetAccountsRequest request) {
        List matches = new ArrayList<String>();
        String regex = request.getTextWildcard();
        if (!regex.isEmpty()) {
            for (String account : allAccounts) {
                if (account.matches(regex)) {
                    matches.add(account);
                }
            }
        } else {
            matches.addAll(allAccounts);
        }
        return new GetAccountsResponse(true, matches);
    }

    /**
     * Creates an account with a given username. If the account exists,
     * returns an unsuccessful Status object. Otherwise, the account
     * is added to `allAccounts` as well as `loggedInUsers`.
     *
     * @param request   A createAcount request, containing a username.
     * @return          A response object indicating whether the operation
     *                  succeeded or failed.
     */
    public CreateAccountResponse createAccountAPI(CreateAccountRequest request) {
        String username = request.getUsername();
        String message;
        Boolean success = false;
        if (allAccounts.contains(username)) {
            message = "User " + username + " already exists.";
        } else {
            this.allAccounts.add(username);
            // Also log in the user.
            this.loggedInUsers.add(username);
            success = true;
            message = "User " + username + " created and logged in successfully.";
        }
        return new CreateAccountResponse(success, message);
    }

    /**
     * Deletes an account from `allAccounts` if the account exists
     * otherwise do nothing.
     * @param request   A deleteAccount request, containing a username.
     * @return          A status object, which is always successful.
     */
    public DeleteUserResponse deleteAccountAPI(DeleteAccountRequest request) {
        String username = request.getUsername();
        String message;
        Boolean success = false;
        if (allAccounts.contains(username)) {
            allAccounts.remove(username);
            // Also remove from logged in users if logged in.
            loggedInUsers.remove(username);

            success = true;
            message = "User " + username + " deleted.";
        } else {
            message = "User " + username + " does not exist and cannot be deleted.";
        }
        return new DeleteUserResponse(success, message);
    }

    /**
     * Gets all messages which are queued for sending for a provided user.
     *
     * @param request   A request for undelivered messages for a user.
     * @return          A response contained the messages.
     */
    public GetUndeliveredMessagesResponse getUndeliveredMessagesAPI(GetUndeliveredMessagesRequest request) {
        String username = request.getUsername();
        if (undeliveredMessages.containsKey(username)) {
            return new GetUndeliveredMessagesResponse(true, undeliveredMessages.get(username));
        } else {
            return new GetUndeliveredMessagesResponse(true, new ArrayList<>());
        }
    }

    /**
     * Sends a message to a user if the user is logged in, otherwise it
     * will add it to the `undeliveredMessages` list to be delivered
     * when the recepient calls `getUndeliveredMessagees`.
     * @param request   A request specifying the message to be sent, including sender and receiver.
     * @return          A status of whether the message was delivered or added to undelivered messages.
     */
    public SendMessageResponse sendMessageAPI(SendMessageRequest request) {
        String sender = request.getSender();
        String recipient = request.getRecipient();
        String strMessage = request.getMessage();

        // Create Message object.
        Message message = new Message(System.currentTimeMillis(), sender, recipient, strMessage);
        if (loggedInUsers.contains(recipient)) {
            // If the user is logged in, immediately send the message.
            addMessageToList(queuedMessagesMap, message);
            return new SendMessageResponse(true, "Message sent successfully.");
        } else {
            // Otherwise add to undelivered messages for future delivery
            addMessageToList(undeliveredMessages, message);
            return new SendMessageResponse(true, "Message queued for delivery.");
        }
    }

    /**
     * Logs in a given user if the user is not already logged in. If the user
     * does not exist, the request fails.
     *
     * @param request   A request specifying the user to log in.
     * @return          A status message indicating success or failure.
     */
    public LoginResponse loginUserAPI(LoginRequest request) {
        String username = request.getUsername();
        if (!allAccounts.contains(username)) {
            return new LoginResponse(false, "User " + username + " does not exist, account " +
                    "must be created before user is logged in.");
        }

        if (loggedInUsers.contains(username)) {
            return new LoginResponse(false, "User " + username + " already logged in.");
        } else {
            loggedInUsers.add(username);
            return new LoginResponse(true, "User " + username + " logged in successfully.");
        }
    }

    /**
     * Logs out a given user, if they are logged in.
     * @param request   A request specifying the user to be logged out.
     * @return          A status message indicating success or failure.
     */
    public LogoutResponse logoutUserAPI(LogoutRequest request) {
        String username = request.getUsername();
        if (loggedInUsers.contains(username)) {
            loggedInUsers.remove(username);
            return new LogoutResponse(true, "User " + username + " logged out successfully.");
        } else {
            return new LogoutResponse(false, "User " + username + " not logged in.");
        }
    }
}
