public class Status {

	private final Boolean success;

	private final String error_message;

    /** 
    @param success Indicates whether or not an operation was performed successfully
    @param error_message Returns error message if operation not performed successfully
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
        Status status = Status(0, "Operation was not performed successfully.");
    }

    public static String displayMessage() {
        return error_message;
    }

    public static String displayStatus() {
        return success;
    }


}