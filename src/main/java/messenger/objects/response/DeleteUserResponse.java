package messenger.objects.response;

public class DeleteUserResponse extends StatusMessageResponse {
    public DeleteUserResponse(Boolean success, String message) {
        super(success, message);
    }
}
