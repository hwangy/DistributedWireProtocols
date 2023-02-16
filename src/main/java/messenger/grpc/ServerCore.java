package messenger.grpc;

import messenger.objects.Message;
import messenger.objects.request.DeleteAccountRequest;
import messenger.objects.request.GetUndeliveredMessagesRequest;
import messenger.objects.request.LoginRequest;
import messenger.objects.request.LogoutRequest;
import messenger.objects.request.SendMessageRequest;
import messenger.objects.response.GetAccountsResponse;
import messenger.objects.response.GetUndeliveredMessagesResponse;
import messenger.util.Logging;

import java.util.*;

public class ServerCore {
    private int nextUserId;
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
        nextUserId = 0;
        this.sentMessages = new HashMap<>();
        this.queuedMessagesMap = new HashMap<>();
        this.undeliveredMessages = new HashMap<>();
        this.loggedInUsers = new HashSet<>();
        this.allAccounts = new HashSet<>();
        this.idToUserMap = new HashMap<>();
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
        String message;
        Boolean success = false;
        if (allAccounts.contains(username)) {
            message = "User " + username + " already exists.";
        } else {
            allAccounts.add(username);
            // Also log in the user.
            loggedInUsers.add(username);
            success = true;
            message = "User " + username + " created and logged in successfully.";
        }
        // Add user to user map
        Logging.logInfo(String.format("Assigning id %d to user %s.", nextUserId, username));
        idToUserMap.put(nextUserId,username);
        Status status = Status.newBuilder().setSuccess(success).setMessage(message).build();
        return LoginReply.newBuilder().setConnectionId(nextUserId++).setStatus(status).build();
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
            // Also remove from logged in users if logged in.
            loggedInUsers.remove(username);

            success = true;
            message = "User " + username + " deleted.";
        } else {
            message = "User " + username + " does not exist and cannot be deleted.";
        }
        // Delete user from user map
        Logging.logInfo(String.format("Deleting user %s.", username));
        idToUserMap.remove(nextUserId, username);
        Status status = Status.newBuilder().setSuccess(success).setMessage(message).build();
        return StatusReply.newBuilder().setStatus(status).build();
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
            /*Logging.logInfo(String.format("Retrieving undelivered messages."));
            Status status = Status.newBuilder().setSuccess(true).setMessage("Retrieving undelivered messages.").build();
            List<Message> messages = undeliveredMessages.get(username);
            return GetUndeliveredMessagesReply.newBuilder().setStatus(status).setMessages(messages).build();
            */
        } else {
            return new GetUndeliveredMessagesResponse(true, new ArrayList<>());
            /*Logging.logInfo(String.format("No undelivered messages."));
            Status status = Status.newBuilder().setSuccess(true).setMessage("No undelivered messages.").build();
            List<Message> messages = new ArrayList<Message>();
            return GetUndeliveredMessagesReply.newBuilder().setStatus(status).setMessages(messages).build();
            */
        }
    }

    /**
     * Sends a message to a user if the user is logged in, otherwise it
     * will add it to the `undeliveredMessages` list to be delivered
     * when the recepient calls `getUndeliveredMessagees`.
     * @param request   A request specifying the message to be sent, including sender and receiver.
     * @return          A status of whether the message was delivered or added to undelivered messages.
     */
    public StatusReply sendMessageAPI(SendMessageRequest request) {
        String sender = request.getSender();
        String recipient = request.getRecipient();
        String strMessage = request.getMessage();

        // Create Message object.
        Message message = new Message(System.currentTimeMillis(), sender, recipient, strMessage);
        if (loggedInUsers.contains(recipient)) {
            // If the user is logged in, immediately send the message.
            addMessageToList(queuedMessagesMap, message);
            //return new SendMessageResponse(true, "Message sent successfully.");
            Logging.logInfo(String.format("Message sent successfully."));
            Status status = Status.newBuilder().setSuccess(true).setMessage("Message sent successfully.").build();
            return StatusReply.newBuilder().setStatus(status).build();
        } else {
            // Otherwise add to undelivered messages for future delivery
            addMessageToList(undeliveredMessages, message);
            //return new SendMessageResponse(true, "Message queued for delivery.");
            Logging.logInfo(String.format("Message queued for delivery."));
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
        String username = request.getUsername();
        if (!allAccounts.contains(username)) {
            //return new LoginResponse(false, "User " + username + " does not exist, account " +
                   // "must be created before user is logged in.");
            Logging.logInfo(String.format( "User %s does not exist, account must be created before user is logged in.", username));
            Status status = Status.newBuilder().setSuccess(false).setMessage("User " + username + " does not exist, account " +
                "must be created before user is logged in.").build();
            return LoginReply.newBuilder().setConnectionId(nextUserId).setStatus(status).build();
        }

        if (loggedInUsers.contains(username)) {
            //return new LoginResponse(false, "User " + username + " already logged in.");
            Logging.logInfo(String.format( "User %s already logged in.", username));
            Status status = Status.newBuilder().setSuccess(false).setMessage( "User " + username + " already logged in.").build();
            return LoginReply.newBuilder().setConnectionId(nextUserId).setStatus(status).build();
        } else {
            loggedInUsers.add(username);
            //return new LoginResponse(true, "User " + username + " logged in successfully.");
            Logging.logInfo(String.format("User %s logged in successfully.", username));
            Status status = Status.newBuilder().setSuccess(true).setMessage("User " + username + " logged in successfully.").build();
            return LoginReply.newBuilder().setConnectionId(nextUserId++).setStatus(status).build();
        }
    }

    /**
     * Logs out a given user, if they are logged in.
     * @param request   A request specifying the user to be logged out.
     * @return          A status message indicating success or failure.
     */
    public StatusReply logoutUserAPI(LogoutRequest request) {
        String username = request.getUsername();
        if (loggedInUsers.contains(username)) {
            loggedInUsers.remove(username);
            //return new LogoutResponse(true, "User " + username + " logged out successfully.");
            Logging.logInfo(String.format("User %s logged out successfully.", username));
            Status status = Status.newBuilder().setSuccess(true).setMessage("User " + username + " logged out successfully.").build();
            return StatusReply.newBuilder().setStatus(status).build();
        } else {
            //return new LogoutResponse(false, "User " + username + " not logged in.");
            Logging.logInfo(String.format("User %s not logged in.", username));
            Status status = Status.newBuilder().setSuccess(false).setMessage("User " + username + " not logged in.").build();
            return StatusReply.newBuilder().setStatus(status).build();
        }
    }
}
