package messenger.objects.response;

public class CreateAccountResponse extends StatusMessageResponse {

    public CreateAccountResponse(Boolean success, String message) {
        super(success, message);
    }
}
