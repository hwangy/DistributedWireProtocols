package messenger.api;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum for valid API calls.
 */
public enum API {
    LOGOUT(0),
    CREATE_ACCOUNT(1),
    GET_ACCOUNTS(2),
    SEND_MESSAGE(3),
    GET_UNDELIVERED_MESSAGES(4),
    DELETE_ACCOUNT(5),
    LOGIN(6);

    private final int identifier;
    private static final Map<Integer, API> intToAPI = new HashMap<Integer, API>();
    static {
        for (API api : API.values()) {
            intToAPI.put(api.identifier, api);
        }
    }

    API(int identifier) {
        this.identifier = identifier;
    }

    public static API fromInt(int identifier) throws APIException {
        if (!intToAPI.containsKey(identifier)) {
            throw new APIException("Identifier " + identifier + " does not correspond" +
                    " to a valid API call.");
        }
        return intToAPI.get(identifier);
    }

    public int getIdentifier() {
        return identifier;
    }
}
