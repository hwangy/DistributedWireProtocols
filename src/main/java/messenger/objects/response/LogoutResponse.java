package messenger.objects.response;

/**
 * Response object for Logout API call
 */
public class LogoutResponse extends StatusMessageResponse {

    /**
     * Specify a LogoutResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public LogoutResponse(Boolean success, String message) {
        super(success, message);
    }

}
