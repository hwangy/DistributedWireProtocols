package messenger.util;

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
}
