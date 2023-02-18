package messenger.grpc;

import messenger.util.Logging;

public class ClientCore {

    // The username associated with the current session
    private String username;

    public ClientCore() {
        this.username = null;
    }

    /**
     * Get the username associated to the ClientCore
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Checks if a user is logged in, by verifying that
     * the username field is set.
     * @return  True if the user is logged in, false otherwise.
     */
    public Boolean isLoggedIn() {
        return this.username != null;
    }

    /**
     * Sets the client's internal status to logged in by updating the username
     * as well as setting the connection ID (based on the login response).
     * @param username  The username to log in
     * @param response  The response from the server
     * @return          Whether the login request was successful or not.
     */
    public Boolean setLoggedInStatus(String username, LoginReply response) {
        Status status = response.getStatus();
        if (status.getSuccess()) {
            // Log in the user with the connection ID retrieved
            // from the server.
            this.username = username;
        }

        Logging.logService(status.getMessage());
        return status.getSuccess();
    }

    /**
     * Sets the client's internal status to logged out by updating the username to null.
     * @param response  The response from the server
     * @return          Whether the logout request was successful or not.
     */
    public Boolean setLoggedOutStatus(StatusReply response) {
        Boolean success = response.getStatus().getSuccess();
        if (success) {
            username = null;
        }

        Logging.logService(response.getStatus().getMessage());
        return success;
    }



}
