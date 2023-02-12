package messenger.api;

public class APIException extends Exception {
    public APIException(String errMessage) {
        super(errMessage);
    }
}
