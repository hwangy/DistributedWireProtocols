package messenger.objects.response;

public class LoginResponse extends StatusMessageResponse {

    public LoginResponse(Boolean success, String message) {
        super(success, message);
    }

}
