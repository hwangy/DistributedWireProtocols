package messenger.util;

/**
 * Holds common constants
 */
public class Constants {
    public static final int API_PORT = 50051;
    public static final int MESSAGE_PORT = 7777;

    public static final int CLIENT_TIMEOUT = 6;

    public static String getUsersFileName(int offset) {
        return "all_users" + offset + ".txt";
    }

    public static String getUndeliveredFileName(int offset) {
        return "undelivered_messages" + offset + ".txt";
    }
}
