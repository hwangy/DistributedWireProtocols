package messenger;

import messenger.objects.Message;
import messenger.objects.Status;
import messenger.objects.request.CreateUserRequest;

import java.util.*;

public class ServerCore {
    private Map<String, ArrayList<Message>> sentMessages;
    private Map<String, ArrayList<Message>> queuedMessages;
    private Set<String> loggedInUsers;
    private Set<String> allUsers;

    public ServerCore() {
       this.sentMessages = new HashMap<>();
       this.queuedMessages = new HashMap<>();
       this.loggedInUsers = new HashSet<>();
       this.allUsers = new HashSet<>();
    }

    /**
     * Fetches all users registered with the server.
     *
     * @return  a list of all users.
     */
    public Set<String> getAllUsers() {
        return allUsers;
    }

    /**
     * Creates a user with a given username. If the user exists,
     * returns an unsuccessful Status object. Otherwise, the user
     * is added to ``allUsers`` as well as ``loggedInUsers``.
     *
     * @param request   A createUser request, containing a username.
     * @return          A status object indicating whether the operation
     *                  succeeded or failed.
     */
    public Status createUser(CreateUserRequest request) {
        String username = request.getUsername();
        if (allUsers.contains(username)) {
            return Status.genFailure("User " + username + " already exists.");
        }
        this.allUsers.add(username);
        return Status.genSuccess();
    }
}
