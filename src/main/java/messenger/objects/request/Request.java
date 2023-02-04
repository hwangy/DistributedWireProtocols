package messenger.objects.request;

import java.util.List;

public class Request {
    int method;
    List<String> arguments;

    public Request(int method, List<String> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public String toString() {
        /*
         To implement
         */
        return "";
    }
}
