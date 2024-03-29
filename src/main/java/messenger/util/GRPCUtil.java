package messenger.util;

import messenger.grpc.Message;
import messenger.grpc.Status;
import messenger.grpc.StatusReply;

import java.text.SimpleDateFormat;

/**
 * Utility functions for constructing and working with GRPC objects.
 */
public class GRPCUtil {
    /**
     * Simple method to turn a Status into a StatusReply.
     * @param status    The status to convert.
     * @return          The StatusReply object
     */
    public static StatusReply replyFromStatus(Status status) {
        return StatusReply.newBuilder().setStatus(status).build();
    }

    /**
     * Create a simple successful StatusReply
     */
    public static StatusReply genSuccessfulReply() {
        return replyFromStatus(Status.newBuilder().setSuccess(true).build());
    }

    private static String formatMessage(Message message) {
        String time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(message.getSentTimestamp());
        return String.format("Received message from [%s] %s:\t%s",
                message.getSender(),time,message.getMessage());
    }

    /**
     * Prints a nicely formatted grpc Message object
     * @param message   The message to print.
     */
    public static void printMessage(Logging logger, Message message) {
        logger.logInfoWithContext(formatMessage(message));
    }

    public static void printMessage(Message message) {
        Logging.logInfo(formatMessage(message));
    }
}
