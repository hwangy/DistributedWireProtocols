package messenger.objects;

import messenger.network.Connection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Message extends Object {
    private final Long timestamp;
    private Long delivered_timestamp;
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
        this.delivered_timestamp = -1L;
    }

    /**
     * Sets the delivered timestamp of a message.
     * @param delivered_timestamp   Timestamp message was delivered
     */
    public void setDeliveredTimestamp(Long delivered_timestamp) {
        this.delivered_timestamp = delivered_timestamp;
    }

    /**
     * Returns the message as a list of strings. Used for
     * converting the message into a response.
     * @return  A list of Strings encoding the message.
     */
    public List<String> asStringList() {
        return Arrays.asList(
                timestamp.toString(),
                delivered_timestamp.toString(),
                sender, receiver, message);
    }

    /**
     * Get the recipient of the message
     * @return The recipient
     */
    public String getRecepient() {
        return receiver;
    }

    /**
     * Get the sender of the message
     * @return The sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get the sent timestamp of the message
     * @return The sent timestamp
     */
    public Long getSentTimestamp() {
        return timestamp;
    }

    /**
     * Get the message contents
     * @return The message contents
     */
    public String getMessage() {
        return message;
    }

    /**
     * Generate a message from the network. This message is blocking
     * and will block until there is data to be read over the Conneciton.
     * @param connection    The connection to read from.
     * @return              The generated message.
     * @throws IOException  Thrown on network exception
     */
    public static Message genMessage(Connection connection) throws IOException {
        Long sentTimestamp = connection.readLong();
        String sender = connection.readString();
        String receiver = connection.readString();
        String message = connection.readString();

        return new Message(sentTimestamp, sender, receiver, message);
    }

    /**
     * Writes a message to the stream over the given Connection.
     * @param connection    The connection to use to send messages.
     * @throws IOException  Thrown on network exception.
     */
    public void writeToStream(Connection connection) throws IOException {
        connection.writeLong(timestamp);
        connection.writeString(sender);
        connection.writeString(receiver);
        connection.writeString(message);
        connection.flushOutput();

        setDeliveredTimestamp(System.currentTimeMillis());
    }

    /**
     * Converts the message into a corresponding String
     * @return A String corresponding to the message
     */
    @Override
    public String toString() {
        return String.format("From %s [%d]: \"%s\"", sender, timestamp, message);
    }
}
