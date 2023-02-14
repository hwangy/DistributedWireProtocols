package messenger.objects.response;

import messenger.api.APIException;
import java.util.Arrays;
import java.util.List;

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

    public StatusMessageResponse(Response response) throws APIException {
        this.success = response.isSuccessful();

        List<String> args = response.getResponses();
        if (args.size() != 1) {
            throw new APIException("StatusMessageResponse expects 1 argument, got " + args.size());
        } else {
            this.message = args.get(0);
        }
    }
    
}
