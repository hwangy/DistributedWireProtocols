package messenger.grpc;

import messenger.util.Logging;

import java.util.*;

public class ServerCore {
    private int nextConnectionId;
    private final Map<String, List<Message>> sentMessages;
    private final Map<String, List<Message>> queuedMessagesMap;

    private final Map<String, List<Message>> undeliveredMessages;
    private final Set<String> loggedInUsers;
    /** idToUserMap is a map from session IDs to users. 
    It stores the current sessions and which users correspond to each session.
    */
    private final Map<Integer, String> idToUserMap;
    private final Set<String> allAccounts;

    public ServerCore() {
        nextConnectionId = 0;
        this.sentMessages = new HashMap<>();
        this.queuedMessagesMap = new HashMap<>();
        this.undeliveredMessages = new HashMap<>();
        this.loggedInUsers = new HashSet<>();
        this.allAccounts = new HashSet<>();
        this.idToUserMap = new HashMap<>();
    }

    /**
     * Logs in a user by adding them to loggedInUsers
     * as well as creating an entry in idToUserMap, with a
     * generated connection id.
     * @param username      The username of user
     * @return              A LoginReply indicating status of the login request.
     */
    private LoginReply logInUser(String username) {
        Boolean success = false;
        Integer connectionId = null;
        String message;
        if (isLoggedIn(username)) {
            message = "User " + username + " is already logged in.";
        } else {
            connectionId = nextConnectionId++;
            Logging.logInfo(String.format("Assigning id %d to user %s.", connectionId, username));
            loggedInUsers.add(username);
            idToUserMap.put(connectionId, username);
            message = "User " + username + " logged in successfully";
        }
        Status status = Status.newBuilder().setSuccess(success).setMessage(message).build();
        if (success) {
            return LoginReply.newBuilder().setConnectionId(connectionId).setStatus(status).build();
        } else {
            return LoginReply.newBuilder().setStatus(status).build();
        }
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
        String recipient = message.getRecipient();
        if (listToAdd.containsKey(recipient)) {
            messageList = listToAdd.get(recipient);
        } else {
            messageList = new ArrayList<>();
            listToAdd.put(recipient, messageList);
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

    /**
     * Fetches all users registered with the server.
     *
     * @return  a list of all users.
     */
    public GetAccountsReply getAccountsAPI(GetAccountsRequest request) {
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
        Status status = Status.newBuilder().setSuccess(true).setMessage("Successfully listing accounts.").build();
        return GetAccountsReply.newBuilder()
                .addAllAccounts(matches)
                .setStatus(status)
                .build();
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
    public LoginReply createAccountAPI(CreateAccountRequest request) {
        String username = request.getUsername();
        if (allAccounts.contains(username)) {
            String message = "User " + username + " already exists.";
            Status status = Status.newBuilder().setSuccess(false).setMessage(message).build();
            return LoginReply.newBuilder().setStatus(status).build();
        } else {
            return logInUser(username);
        }
    }

    /**
     * Deletes an account from `allAccounts` if the account exists
     * otherwise do nothing.
     * @param request   A deleteAccount request, containing a username.
     * @return          A status object, which is always successful.
     */
    public StatusReply deleteAccountAPI(DeleteAccountRequest request) {
        String username = request.getUsername();
        String message;
        Boolean success = false;
        if (allAccounts.contains(username)) {
            allAccounts.remove(username);
            // Also remove from logged-in users if logged in.
            loggedInUsers.remove(username);

            success = true;
            message = "User " + username + " deleted.";
        } else {
            message = "User " + username + " does not exist and cannot be deleted.";
        }
        // Delete user from user map
        Logging.logInfo(String.format("Deleting user %s.", username));
        idToUserMap.remove(request.getConnectionId());
        Status status = Status.newBuilder().setSuccess(success).setMessage(message).build();
        return StatusReply.newBuilder().setStatus(status).build();
    }

    /**
     * Gets all messages which are queued for sending for a provided user.
     *
     * @param request   A request for undelivered messages for a user.
     * @return          A response contained the messages.
     */
    public GetUndeliveredMessagesReply getUndeliveredMessagesAPI(GetUndeliveredMessagesRequest request) {
        String username = request.getUsername();
        List<Message> messages;
        if (undeliveredMessages.containsKey(username)) {
            messages = undeliveredMessages.get(username);
        } else {
            messages = new ArrayList<>();
        }
        Status status = Status.newBuilder().setSuccess(true).setMessage("Retrieving undelivered messages.").build();
        return GetUndeliveredMessagesReply.newBuilder().setStatus(status).addAllMessages(messages).build();
    }

    /**
     * Sends a message to a user if the user is logged in, otherwise it
     * will add it to the `undeliveredMessages` list to be delivered
     * when the recepient calls `getUndeliveredMessagees`.
     * @param request   A request specifying the message to be sent, including sender and receiver.
     * @return          A status of whether the message was delivered or added to undelivered messages.
     */
    public StatusReply sendMessageAPI(SendMessageRequest request) {
        Message message = request.getMessage();

        if (loggedInUsers.contains(message.getRecipient())) {
            // If the user is logged in, immediately send the message.
            addMessageToList(queuedMessagesMap, message);
            Status status = Status.newBuilder().setSuccess(true).setMessage("Message sent successfully.").build();
            return StatusReply.newBuilder().setStatus(status).build();
        } else {
            // Otherwise add to undelivered messages for future delivery
            addMessageToList(undeliveredMessages, message);
            Status status = Status.newBuilder().setSuccess(true).setMessage("Message queued for delivery.").build();
            return StatusReply.newBuilder().setStatus(status).build();
        }
    }

    /**
     * Logs in a given user if the user is not already logged in. If the user
     * does not exist, the request fails.
     *
     * @param request   A request specifying the user to log in.
     * @return          A status message indicating success or failure.
     */
    public LoginReply loginUserAPI(LoginRequest request) {
        return logInUser(request.getUsername());
    }

    /**
     * Logs out a given user, if they are logged in.
     * @param request   A request specifying the user to be logged out.
     * @return          A status message indicating success or failure.
     */
    public StatusReply logoutUserAPI(LogoutRequest request) {
        Boolean success = false;
        String username = request.getUsername();
        String message;
        if (!isLoggedIn(username)) {
            message = "User " + username + " is not logged in and cannot be logged out.";
        } else {
            loggedInUsers.remove(username);
            idToUserMap.remove(request.getConnectionId());
            message = "User " + username + " logged out successfully.";
        }
        return StatusReply.newBuilder().setStatus(Status.newBuilder()
                .setSuccess(success)
                .setMessage(message).build()).build();
    }
}
