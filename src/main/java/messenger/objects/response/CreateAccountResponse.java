package messenger.objects.response;

/**
 * Response object for CreateAccount API call
 */
public class CreateAccountResponse extends StatusMessageResponse {

    /**
     * Specify a CreateAccountResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public CreateAccountResponse(Boolean success, String message) {
        super(success, message);
    }
}
