package src.main.java.Messenger.objects;

public class Message {
    private final int timestamp;
    private int delivered_timestamp;
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
    public Message(int timestamp, String sender,
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
}
