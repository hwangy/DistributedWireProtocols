package messenger.objects.response;

public class LogoutResponse extends StatusMessageResponse {

    public LogouResponse(Boolean success, String message) {
        super(success, message);
    }
}
