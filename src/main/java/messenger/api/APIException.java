package messenger.api;

/**
 * Class which represents API-level exceptions encountered by
 * the Server or Client. For example, if not enough arguments
 * are received by the Server for a given API call.
 */
public class APIException extends Exception {
    public APIException(String errMessage) {
        super(errMessage);
    }
}
