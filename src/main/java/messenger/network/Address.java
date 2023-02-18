package messenger.network;

/**
 * A helper class which abstracts the IP address and port
 * into a single object. It also knows how to represent
 * itself as a string.
 */
public class Address {
    private final String ipAddress;
    private final int port;

    public Address(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    /**
     * Returns a String formatted for GRPC's ChannelBuilders.
     * @return  A formatted network address string
     */
    public String toString() {
        return String.format("%s:%d", ipAddress, port);
    }
}
