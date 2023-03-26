package messenger.grpc;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import messenger.util.Logging;

public class ClientCore {

    // The username associated with the current session
    private String username;

    // Connection status of the client
    private Boolean connectionEstablished;

    // A boolean flag to signal the client should exit
    private Boolean exit = false;

    private ManagedChannel channel;

    private Boolean isPrimary = false;

    public ClientCore() {
        username = null;
        connectionEstablished = false;
        channel = null;
    }

    public ClientCore(ManagedChannel channel) {
        username = null;
        connectionEstablished = false;
        this.channel = channel;
    }

    public Boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    /**
     * Returns the current channel used by the client
     * @return  A ManagedChannel
     */
    public ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Sets a new channel, e.g. if the prevous server has disconnected.
     * @param channel
     */
    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public Boolean isChannelReady() {
        return channel.getState(false).equals(ConnectivityState.READY);
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

    public Boolean setLoggedOutStatus() {
        return setLoggedOutStatus(StatusReply.newBuilder()
                .setStatus(Status.newBuilder().setSuccess(true).build()).build());
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

    /**
     * Set the internal status of the client to connected.
     */
    public void setConnected() {
        this.connectionEstablished = true;
    }

    /**
     * Set the internal status of the client to disconnected.
     */
    public void setDisconnected()  {
        this.connectionEstablished = false;
    }

    /**
     * Gets the connection status of the client.
     *
     * @return  True if connected, false otherwise
     */
    public Boolean getConnectionStatus() {
        return this.connectionEstablished;
    }

    public void setExit() {
        exit = true;
    }

    public Boolean getExit() {
        return exit;
    }

}
