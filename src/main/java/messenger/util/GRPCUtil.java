package messenger.util;

import messenger.grpc.Status;
import messenger.grpc.StatusReply;

public class GRPCUtil {
    /**
     * Simple method to turn a Status into a StatusReply.
     * @param status    The status to convert.
     * @return          The StatusReply object
     */
    public static StatusReply relyFromStatus(Status status) {
        return StatusReply.newBuilder().setStatus(status).build();
    }
}
