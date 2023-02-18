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

    /**
     * Generate a success Status object
     * @return A Status object corresponding to success
     */
    public static Status genSuccess() {
        return new Status(true, "");
    }

    /** 
     * Generate a failure Status object
     * @param error_message The error message to be displayed
     * @return A Status object corresponding to the failure
     */
    public static Status genFailure(String error_message) {
        return new Status(false, error_message);
    }

    /**
     * Get the error message
     * @return The error message
     */
    public String getMessage() {
        return this.error_message;
    }

    /**
     * Get the indicator of if there was success
     * @return  Indicator of if there was success
     */
    public Boolean isSuccess() {
        return this.success;
    }


}