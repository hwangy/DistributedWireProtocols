package messenger.objects.request;

import java.util.ArrayList;

public class CreateUserRequest implements MethodRequestInterface {
    @Override
    public Request genGenericRequest() {
        return new Request(1, new ArrayList<String>());
    }
}
