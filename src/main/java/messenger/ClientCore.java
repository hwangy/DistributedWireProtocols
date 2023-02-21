package messenger;

import messenger.objects.request.*;
import messenger.objects.response.*;
import messenger.util.Logging;

public class ClientCore {
    private String username;

    public ClientCore() {
        this.username = null;
    }

    /**
     * Get the username
     * @return The username
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
     * If the login response tells us that the server has succeeded in logging in, 
      update the username in ClientCore. Else return a failure message.
    *  @param request The request object
    *  @param response The response object
    @* @return An indicator of if there was success
    */
    public Boolean loginAPI(LoginRequest request, StatusMessageResponse response) {
        Boolean success = response.isSuccessful();
        if (success) {
            this.username = request.getUsername();
        } else {
            Logging.logService("Failed to log in.");
        }
        return success;
    }

    /**
     * Update the information on the client's side that the user is logging out.
     * Updates the username that the client stores to null
     * @param response The response object
     * @return And indicator of if there was success
     */
    public Boolean logoutAPI(StatusMessageResponse response) {
        Boolean success = response.isSuccessful();
        if(success) {
            this.username = null;
        } else {
            Logging.logService("Failed to log out.");
        }
        return success;
    }



}
