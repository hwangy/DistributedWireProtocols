package messenger.objects.request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class Request {
    int method;
    List<String> arguments;

    public Request(int method, List<String> arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    public void writeToStream(DataOutputStream io) throws IOException {
        /*
         To implement
         */
        io.writeInt(method);
        for (String argument : arguments) {
            io.writeUTF(argument);
        }
    }
}
