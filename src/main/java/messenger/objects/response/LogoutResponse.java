package messenger.objects.response;

public class LogoutResponse extends StatusMessageResponse {

    public LogoutResponse(Boolean success, String message) {
        super(success, message);
    }
}
