package messenger.api;

import java.util.HashMap;
import java.util.Map;

/**
 * An enum for valid API calls.
 */
public enum API {
    LOGOUT(0),
    CREATE_ACCOUNT(1),
    LOGIN(2),
    GET_ACCOUNTS(3),
    SEND_MESSAGE(4),
    GET_UNDELIVERED_MESSAGES(5),
    DELETE_ACCOUNT(6);

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

    /**
     * Create an API enum from an integer identifier.
     * @param identifier        A numerical identifier for an API call
     * @return                  The corresponding API enum
     * @throws APIException     Thrown if the identifier does not correspond to
     *                          an API call.
     */
    public static API fromInt(int identifier) throws APIException {
        if (!intToAPI.containsKey(identifier)) {
            throw new APIException("Identifier " + identifier + " does not correspond" +
                    " to a valid API call.");
        }
        return intToAPI.get(identifier);
    }

    /**
     * Returns the integer identifier.
     * @return  The identifier for the enum.
     */
    public int getIdentifier() {
        return identifier;
    }
}
