package messenger.objects;

import java.util.Arrays;
import java.util.List;

public class Message extends Object {
    private final Long timestamp;
    private Integer delivered_timestamp;
    private final String sender;
    private final String receiver;
    private final String message;

    /**
     * Initialize undelivered message. The undelivered status is indicated
     * by setting `delivered_timestamp` to -1.
     *
     * @param timestamp Timestamp message was created
     * @param sender    Username of sender
     * @param receiver  Username of receiver
     * @param message   Message contents
     */
    public Message(Long timestamp, String sender,
                   String receiver, String message){
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;

        // Set to undelivered
        this.delivered_timestamp = -1;
    }

    /**
     * Sets the delivered timestamp of a message.
     * @param delivered_timestamp   Timestamp message was delivered
     */
    public void setDeliveredTimestamp(int delivered_timestamp) {
        this.delivered_timestamp = delivered_timestamp;
    }

    public List<String> asStringList() {
        return Arrays.asList(
                timestamp.toString(),
                delivered_timestamp.toString(),
                sender, receiver, message);
    }
}
