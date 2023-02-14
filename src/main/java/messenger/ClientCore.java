package messenger;

import messenger.objects.Message;
import messenger.objects.request.*;
import messenger.objects.response.*;
import messenger.util.Logging;

import java.util.*;

public class ClientCore {
    private String username;

    public ClientCore() {
       this.username = "";
    }

    public String getUsername() {
        return this.username;
    }

    /* If the login response tells us that the server has succeeded in logging in, 
      update the username in ClientCore. Else return a failure message.
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
