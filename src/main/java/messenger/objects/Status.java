package messenger.objects;

public class Status {

	private final Boolean success;

	private final String error_message;

    /**
     * Return object for calls which may fail
     *
     * @param success Indicates whether an operation was performed successfully
     * @param error_message Returns error message if operation not performed successfully
     */
    public Status(Boolean success, String error_message) {
        this.success = success;
        this.error_message = error_message;
    }

    public static Status genSuccess() {
        return new Status(true, "");
    }

    /** 
    @param error_message The error message to be displayed
     */
    public static Status genFailure(String error_message) {
        return new Status(false, error_message);
    }

    public String displayMessage() {
        return this.error_message;
    }

    public Boolean displayStatus() {
        return this.success;
    }


}