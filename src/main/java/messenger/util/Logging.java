package messenger.util;

public class Logging {
    public static void logInfo(String toLog) {
        System.out.printf("[INFO]\t%s\n", toLog);
    }

    public static void logDebug(String toLog) {
        System.out.printf("[DEBUG]\t%s\n", toLog);
    }

    public static void logService(String toLog) {
        System.out.printf("[SERVICE]\t%s\n", toLog);
    }
}
