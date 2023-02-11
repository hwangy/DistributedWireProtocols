package messenger;

import messenger.objects.Message;
import messenger.objects.Status;
import messenger.objects.request.CreateAccountRequest;
import messenger.objects.request.DeleteAccountRequest;

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

    /**
     * Fetches all users registered with the server.
     *
     * @return  a list of all users.
     */
    public Set<String> getAllAccounts() {
        return allAccounts;
    }

    /**
     * Creates an account with a given username. If the account exists,
     * returns an unsuccessful Status object. Otherwise, the account
     * is added to `allAccounts` as well as `loggedInUsers`.
     *
     * @param request   A createAcount request, containing a username.
     * @return          A status object indicating whether the operation
     *                  succeeded or failed.
     */
    public Status createAccount(CreateAccountRequest request) {
        String username = request.getUsername();
        if (allAccounts.contains(username)) {
            return Status.genFailure("User " + username + " already exists.");
        }
        this.allAccounts.add(username);
        return Status.genSuccess();
    }

    /**
     * Deletes an account from `allAccounts` if the account exists
     * otherwise do nothing.
     * @param request   A deleteAccount request, containing a username.
     * @return          A status object, which is always successful.
     */
    public Status deleteAccount(DeleteAccountRequest request) {
        String username = request.getUsername();
        allAccounts.remove(username);
        return Status.genSuccess();
    }
}
