package messenger.objects.request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class Request {
    private int methodId;
    private List<String> arguments;

    public Request(int methodId, List<String> arguments) {
        this.methodId = methodId;
        this.arguments = arguments;
    }

    public int getMethodId() {
        return this.methodId;
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public void writeToStream(DataOutputStream io) throws IOException {
        /*
         To implement
         */
        io.writeInt(methodId);
        for (String argument : arguments) {
            io.writeUTF(argument);
        }
    }
}
