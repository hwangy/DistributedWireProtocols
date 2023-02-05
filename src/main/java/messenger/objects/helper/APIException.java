package messenger.objects.helper;

public class APIException extends Exception {
    public APIException(String errMessage) {
        super(errMessage);
    }
}
