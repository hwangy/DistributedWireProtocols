package messenger.network;

import messenger.util.Constants;

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

    public String toString() {
        return String.format("%s:%d", ipAddress, port);
    }
}
