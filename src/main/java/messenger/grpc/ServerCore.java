package messenger.grpc;

import messenger.network.Address;
import messenger.util.Constants;
import messenger.util.Logging;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerCore {
    // Maintain a map of usernames to lists of sent messages
    private final Map<String, List<Message>> sentMessages;
    // A map from recipients to messages which should be delivered immediately. This list
    // is monitored by client/username-specific threads
    private final Map<String, List<Message>> queuedMessagesMap;

    // A map of usernames to their undelivered messages
    private final Map<String, List<Message>> undeliveredMessages;
    // All currently logged-in users
    private final Map<String, Address> loggedInUsers;
    /**
     * Stores a map of connected IP addresses as well as ports. This allows
     * the server to assign a unique port if the IP address is repeated
     */
    private final Map<String, List<Integer>> ipToPorts;
    // All created and not deleted accounts.
    private final Set<String> allAccounts;

    // FileWriter to write to the all_users file
    FileWriter usersWriter;
    // FileWriter to write to the undelivered_messages file
    FileWriter undeliveredMessagesWriter;

    // Keeps track of whether the current server is the primary
    private Boolean isPrimary = false;

    private final int offset;

    public ServerCore() {
        sentMessages = new HashMap<>();
        queuedMessagesMap = new HashMap<>();
        undeliveredMessages = new HashMap<>();
        loggedInUsers = new HashMap<>();
        allAccounts = new HashSet<>();
        ipToPorts = new HashMap<>();
        this.offset = 0;
    }

    public ServerCore(int offset) {
        sentMessages = new HashMap<>();
        queuedMessagesMap = new HashMap<>();
        undeliveredMessages = new HashMap<>();
        loggedInUsers = new HashMap<>();
        allAccounts = new HashSet<>();
        ipToPorts = new HashMap<>();
        this.offset = offset;

        // Create the initialization of the all users and undelivered messages files
        try {
            File allUsersFile = new File(Constants.getUsersFileName(offset));
            // Creates file if doesn't exist
            allUsersFile.createNewFile();
            File undeliveredMsgsFile = new File(Constants.getUndeliveredFileName(offset));
            // Creates file if doesn't exist
            undeliveredMsgsFile.createNewFile();

            BufferedReader usersReader = new BufferedReader(new FileReader(allUsersFile));
            BufferedReader undeliveredMessagesReader = new BufferedReader(new FileReader(undeliveredMsgsFile));
            Gson gson = new Gson();
            String userList = usersReader.readLine();
            String undeliveredMsgList = undeliveredMessagesReader.readLine();

            if (userList == null) {
                usersWriter = new FileWriter(Constants.getUsersFileName(offset), false);
                String jsonAccts = gson.toJson(allAccounts);
                usersWriter.write(jsonAccts);
                usersWriter.close();
            } else {
                // Add existing accounts in all_users to allAccounts (the list of accounts that exist)
                this.allAccounts.addAll(gson.fromJson(userList, new TypeToken<HashSet<String>>(){}.getType()));
            }
            
            if (undeliveredMsgList == null) {
                usersWriter = new FileWriter(Constants.getUndeliveredFileName(offset), false);
                String jsonMsgs = gson.toJson(undeliveredMessages);
                usersWriter.write(jsonMsgs);
                usersWriter.close();
            } else {
                // Add existing accounts in all_users to allAccounts (the list of accounts that exist)
                this.undeliveredMessages.putAll(gson.fromJson(undeliveredMsgList, new TypeToken<HashMap<String, List<Message>>>(){}.getType()));
            }
            
            
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Method to add a user to the list of all users and update the file storing all users
     * @param username the username
     */
    public void addUser(String username) {
        try {
            usersWriter = new FileWriter(Constants.getUsersFileName(offset), false);
            allAccounts.add(username);
            Gson gson = new Gson();
            String json = gson.toJson(allAccounts);
            usersWriter.write(json);
            usersWriter.close();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Method to delete a user from the list of all users and update the file storing all users
     * @param username the username
     */
    public void deleteUser(String username) {
        try {
            usersWriter = new FileWriter(Constants.getUsersFileName(offset), false);
            allAccounts.remove(username);
            Gson gson = new Gson();
            String json = gson.toJson(allAccounts);
            usersWriter.write(json);
            usersWriter.close();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Method to check if a username exists in the list of all accounts
     * @param username the username
     */
    public Boolean usernameExists (String username) {
        return allAccounts.contains(username);
    }

    /**
     * Method to remove a message from the map of all undelivered messages and update the file storing all undelivered messages
     * @param username the username
     */
    public void removeUndeliveredMessage(String username) {
        try {
            undeliveredMessagesWriter = new FileWriter(Constants.getUndeliveredFileName(offset), false);
            undeliveredMessages.remove(username);
            Gson gson = new Gson();
            String json = gson.toJson(undeliveredMessages);
            undeliveredMessagesWriter.write(json);
            undeliveredMessagesWriter.close();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    /**
     * Method to see if the map of all undelivered messages contains a certain key (which is a username)
     * @param username the username
     */
    public Boolean undeliveredMessageContainsKey(String username) {
        return undeliveredMessages.containsKey(username);
    }

    /**
     * Method to get undelivered messages for a specified username from the map of all undelivered messages
     * @param username the username
     */
    public List<Message> getUndeliveredMessages(String username) {
        List<Message> messages;
        messages = undeliveredMessages.get(username);
        return messages;
    }

    /**
     * Method to get the undelivered messages map
     */
    public Map<String, List<Message>> getUndeliveredMessagesMap() {
        return undeliveredMessages;
    }

    /**
     * Method to add a message to the map of all undelivered messages and update the file storing all undelivered messages
     * @param message the message
     */
    public void addUndeliveredMessage(Message message) {
        try {
            addMessageToList(undeliveredMessages, message);
            undeliveredMessagesWriter = new FileWriter(Constants.getUndeliveredFileName(offset), false);
            Gson gson = new Gson();
            String json = gson.toJson(undeliveredMessages);
            undeliveredMessagesWriter.write(json);
            undeliveredMessagesWriter.close();
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        }
    }

    private void setPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public Boolean isPrimary() {
        return isPrimary;
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
        ListIterator<Integer> it = assignedPorts.listIterator();
        while (it.hasNext()) {
            Integer currPort = it.next();
            if (currPort <= port) {
                // If the next port in the list is smaller than
                // the current port we're attempting to add, then
                // we should move further down the list.
                port++;
            } else {
                // Otherwise, insert before the current element.
                it.previous();
                break;
            }
        }
        it.add(port);
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
        return nextPortInList(ports);
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

    /**
     * Get all accounts
     * @return Return all accounts
     */
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

    /**
     * Return the queued messages for a particular user, specified by the username
     * @param username The username
     * @return Optionally return the list of queued messages
     */
    public Optional<List<Message>> getQueuedMessages(String username) {
        if (queuedMessagesMap.containsKey(username)) {
            return Optional.of(queuedMessagesMap.get(username));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Unqueue the messages for a particular user, specified by the username
     * @param username The username
     */
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

    /**
     * Returns whether a user is logged in 
     * @param username The username
     * @return Indicator of if user is logged in
     */
    public Boolean isLoggedIn(String username) {
        return loggedInUsers.containsKey(username);
    }

    public StatusReply markAsPrimaryAPI(SetPrimaryRequest request) {
        setPrimary(true);
        return StatusReply.newBuilder().setStatus(
                Status.newBuilder().setSuccess(true).setMessage("Server set as primary")).build();
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
            //allAccounts.add(username);
            addUser(username);

            Status status = Status.newBuilder().setSuccess(true).setMessage("User created successfully.").build();
            return LoginReply.newBuilder().setStatus(status).build();
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
            //allAccounts.remove(username);
            deleteUser(username);
            // Also clear undelivered messages
            //undeliveredMessages.remove(username);
            removeUndeliveredMessage(username);
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
        /*if (undeliveredMessages.containsKey(username)) {
            messages = undeliveredMessages.get(username);
        } else {
            messages = new ArrayList<>();
        }*/
        if (undeliveredMessageContainsKey(username)) {
            messages = getUndeliveredMessages(username);
        } else {
            messages = new ArrayList<>();
        }
        Status status = Status.newBuilder().setSuccess(true).setMessage("Retrieving undelivered messages.").build();

        // Clear the undelivered messages
        //undeliveredMessages.remove(username);
        removeUndeliveredMessage(username);
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
            addUndeliveredMessage(message);
            //addMessageToList(undeliveredMessages, message);
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
