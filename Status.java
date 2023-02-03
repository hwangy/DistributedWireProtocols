public class Status {

	private final Boolean success;

	private final String error_message;

    /** 
    @param success Indicates whether or not a message was sent successfully
    @param error_message Returns error message if message was not sent successfully
    */

    public Status(Boolean success, String error_message) {
        this.success = success;
        this.error_message = error_message;
    }


    public static Status genSuccess() {
        Status status = Status(1, "");
        return status;
    }

    public static Status genFailure() {
        Status status = Status(0, "Message was not delivered successfully.");
    }


}