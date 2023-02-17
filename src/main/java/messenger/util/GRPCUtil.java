package messenger.util;

import messenger.grpc.Message;
import messenger.grpc.Status;
import messenger.grpc.StatusReply;

import java.text.SimpleDateFormat;

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
        String time = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(message.getSentTimestamp());
        Logging.logInfo(String.format("Received message from [%s] %s:\t%s",
                message.getSender(),time,message.getMessage()));
    }
}
