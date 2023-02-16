package messenger.grpc;

import messenger.util.Logging;

public class ClientCore {

    // The username associated with the current session
    private String username;
    // The connection ID associated with the current session.
    // This value is assigned by the Server.
    private Integer connectionId;

    public ClientCore() {
        this.username = null;
        this.connectionId = null;
    }

    public String getUsername() {
        return this.username;
    }

    public Integer getConnectionId() {
        return this.connectionId;
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
     * Sets the clients internal status to logged in by updating the username
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
            connectionId = response.getConnectionId();
        }

        Logging.logService(status.getMessage());
        return status.getSuccess();
    }

    public Boolean setLoggedOutStatus(StatusReply response) {
        Boolean success = response.getStatus().getSuccess();
        if(success) {
            this.username = null;
            this.connectionId = null;
        }

        Logging.logService(response.getStatus().getMessage());
        return success;
    }



}