package messenger.objects.response;

public class SendMessageResponse extends StatusMessageResponse {
    public SendMessageResponse(Boolean success, String message) {
        super(success, message);
    }
}
