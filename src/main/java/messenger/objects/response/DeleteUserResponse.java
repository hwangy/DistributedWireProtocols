package messenger.objects.response;

/**
 * Response object for DeleteAccount API call
 */
public class DeleteUserResponse extends StatusMessageResponse {
    
    /**
     * Specify a DeleteAccountResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public DeleteUserResponse(Boolean success, String message) {
        super(success, message);
    }
}
