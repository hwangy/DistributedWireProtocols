package messenger.grpc;

import messenger.network.Address;
import messenger.util.Constants;
import messenger.util.Logging;

import java.util.*;

public class ServerCore {
    private final Map<String, List<Message>> sentMessages;
    private final Map<String, List<Message>> queuedMessagesMap;

    private final Map<String, List<Message>> undeliveredMessages;
    private final Map<String, Address> loggedInUsers;
    /**
     * Stores a map of connected IP addresses as well as ports. This allows
     * the server to assign a unique port if the IP address is repeated
     */
    private final Map<String, List<Integer>> ipToPorts;
    private final Set<String> allAccounts;

    public ServerCore() {
        this.sentMessages = new HashMap<>();
        this.queuedMessagesMap = new HashMap<>();
        this.undeliveredMessages = new HashMap<>();
        this.loggedInUsers = new HashMap<>();
        this.allAccounts = new HashSet<>();
        this.ipToPorts = new HashMap<>();
    }

    /**
     * Returns the next port to assign to a user, assuming
     * the other ports in the list have been assigned. This
     * method assumes incoming list is sorted. Thus, this method
     * just needs to find the first "gap" in the list of ports.
     * @param assignedPorts The list of ports to check
     * @return              The next unassigned port.
     */
    private int nextPortInList(List<Integer> assignedPorts) {
        int port = Constants.MESSAGE_PORT;
        for (Integer currentPort : assignedPorts) {
            if (currentPort == port) {
                port++;
            } else {
                break;
            }
        }
        return port;
    }

    /**
     * Returns the next available port on the IP Address
     * @param ipAddress The ip address to check
     * @return          Next available port, starting from Constants.MESSAGE_PORT
     */
    private int assignPort(String ipAddress) {
        List<Integer> ports;
        if (ipToPorts.containsKey(ipAddress)) {
            ports = ipToPorts.get(ipAddress);
        } else {
            ports = new ArrayList<>();
            ipToPorts.put(ipAddress, ports);
        }
        int nextPort = nextPortInList(ports);
        ports.add(nextPort);
        return nextPort;
    }

    /**
     * Removes the given user from the logged in users by removing
     * their stored connection information (ip address and port). This
     * also frees up the port for future connections on the same port.
     * @param username  The username to log out.
     */
    private void removeUserConnection(String username) {
        if (!loggedInUsers.containsKey(username)) return;
        Address address = loggedInUsers.remove(username);
        List<Integer> ports = ipToPorts.get(address.getIpAddress());
        ports.remove((Integer) address.getPort());
        if (ports.size() == 0) {
            // If the size of the ports list is 0 after
            // removing this port, remove the IP address from
            // the list
            ipToPorts.remove(address.getIpAddress());
        }
    }

    /**
     * Logs in a user by adding them to loggedInUsers
     * as well as creating an entry in idToUserMap, with a
     * generated connection id.
     * @param username      The username of user
     * @param ipAddress     The ip address of the user
     * @return              A LoginReply indicating status of the login request.
     */
    private LoginReply logInUser(String username, String ipAddress) {
        String message;
        if (!allAccounts.contains(username)) {
            message = "User " + username + " does not exist and cannot be logged in.";
            return LoginReply.newBuilder()
                    .setStatus(Status.newBuilder().setSuccess(false).setMessage(message).build())
                    .build();
        } else if (isLoggedIn(username)) {
            message = "User " + username + " is already logged in.";
            return LoginReply.newBuilder()
                    .setStatus(Status.newBuilder().setSuccess(false).setMessage(message).build())
                    .build();
        } else {
            int port = assignPort(ipAddress);
            Logging.logInfo(String.format("Assigning port %d to user %s.", port, username));
            loggedInUsers.put(username, new Address(ipAddress, port));
            message = "User " + username + " logged in successfully";
            return LoginReply.newBuilder()
                    .setReceiverPort(port)
                    .setStatus(Status.newBuilder().setSuccess(true).setMessage(message).build())
                    .build();
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
        return loggedInUsers.containsKey(username);
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
            allAccounts.add(username);
            return logInUser(username, request.getIpAddress());
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
            removeUserConnection(username);

            success = true;
            message = "User " + username + " deleted.";
        } else {
            message = "User " + username + " does not exist and cannot be deleted.";
        }
        // Delete user from user map
        Logging.logInfo(String.format("Deleting user %s.", username));
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

        Status status;
        if (!allAccounts.contains(message.getRecipient())) {
            status = Status.newBuilder().setSuccess(false).setMessage("Recipient does not exist.").build();
        } else if (loggedInUsers.containsKey(message.getRecipient())) {
            // If the user is logged in, immediately send the message.
            addMessageToList(queuedMessagesMap, message);
            status = Status.newBuilder().setSuccess(true).setMessage("Message sent successfully.").build();
        } else {
            // Otherwise add to undelivered messages for future delivery
            addMessageToList(undeliveredMessages, message);
            status = Status.newBuilder().setSuccess(true).setMessage("Message queued for delivery.").build();
        }
        return StatusReply.newBuilder().setStatus(status).build();
    }

    /**
     * Logs in a given user if the user is not already logged in. If the user
     * does not exist, the request fails.
     *
     * @param request   A request specifying the user to log in.
     * @return          A status message indicating success or failure.
     */
    public LoginReply loginUserAPI(LoginRequest request) {
        return logInUser(request.getUsername(), request.getIpAddress());
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
            removeUserConnection(username);
            message = "User " + username + " logged out successfully.";
        }
        return StatusReply.newBuilder().setStatus(Status.newBuilder()
                .setSuccess(success)
                .setMessage(message).build()).build();
    }
}
