package messenger;

import messenger.objects.Message;
import messenger.objects.request.*;
import messenger.objects.response.*;

import java.util.*;

public class ClientCore {
    private String username;

    public ClientCore() {
       this.username = "";
    }

    public String getUsername() {
        return this.username;
    }

    /*public LoginResponse setUsername(String username) {
        return 
    }*/

    // actually should be in server?
    /*public LoginResponse setLoginUsername(LoginRequest request) {
        String username = request.getUsername();
    }*/

}
