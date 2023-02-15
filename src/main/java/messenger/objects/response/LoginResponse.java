package messenger.objects.response;

/**
 * Response object for Login API call
 */
public class LoginResponse extends StatusMessageResponse {

    /**
     * Specify a LoginResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public LoginResponse(Boolean success, String message) {
        super(success, message);
    }

}
