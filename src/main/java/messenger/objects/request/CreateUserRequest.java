package messenger.objects.request;

import messenger.objects.helper.API;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateUserRequest implements MethodRequestInterface {
    private final String username;

    public CreateUserRequest(String username) {
        this.username = username;
    }

    @Override
    public int getIdentifier() {
        return API.CREATE_USER.getIdentifier();
    }
    @Override
    public Request genGenericRequest() {
        return new Request(getIdentifier(), Arrays.asList(new String[]{username}));
    }
}
