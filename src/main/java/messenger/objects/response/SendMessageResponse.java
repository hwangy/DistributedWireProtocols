package messenger.objects.response;

/**
 * Response object for SendMessage API call
 */
public class SendMessageResponse extends StatusMessageResponse {
   
   /**
     * Specify a SendMessageResponse with the given
     * success and message parameters.
     * @param success Indicates if there was a success
     * @param message The message
     */
    public SendMessageResponse(Boolean success, String message) {
        super(success, message);
    }
}
