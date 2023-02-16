package messenger.util;

import messenger.grpc.Message;
import messenger.grpc.Status;
import messenger.grpc.StatusReply;

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

    public static void printMessage(Message message) {
        Logging.logInfo(String.format("Received message from [%s] %d:\t%s",
                message.getSender(),message.getSentTimestamp(),message.getMessage()));
    }
}
