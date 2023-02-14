package messenger;

import messenger.objects.Message;
import messenger.objects.request.*;
import messenger.objects.response.*;

import java.util.*;

public class ServerCore {
    private Map<String, ArrayList<Message>> sentMessages;
    private Map<String, ArrayList<Message>> queuedMessages;
    private Set<String> loggedInUsers;
    private Set<String> allAccounts;

    public ServerCore() {
       this.sentMessages = new HashMap<>();
       this.queuedMessages = new HashMap<>();
       this.loggedInUsers = new HashSet<>();
       this.allAccounts = new HashSet<>();
    }

    public Set<String> getAccounts() {
        return allAccounts;
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
            success = true;
            message = "User " + username + " created successfully.";
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
        if (queuedMessages.containsKey(username)) {
            return new GetUndeliveredMessagesResponse(true, queuedMessages.get(username));
        } else {
            return new GetUndeliveredMessagesResponse(true, new ArrayList<>());
        }
    }

    public SendMessageResponse sendMessageAPI(SendMessageRequest request) {
        String sender = request.getSender();
        String recipient = request.getRecipient();
        String strMessage = request.getMessage();

        // Create Message object.
        Message message = new Message(System.currentTimeMillis(), sender, recipient, strMessage);
        //TODO: Fix
        return null;
    }
}
