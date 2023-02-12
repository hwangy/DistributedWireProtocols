package messenger.objects.response;

import java.util.Arrays;

public class StatusMessageResponse implements MethodResponseInterface {
    private final Boolean success;
    private final String message;

    public StatusMessageResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean isSuccessful() {
        return success;
    }

    @Override
    public String getStringStatus() {
        return message;
    }

    @Override
    public Response genGenericResponse() {
        return new Response(success, Arrays.asList(message));
    }
}
