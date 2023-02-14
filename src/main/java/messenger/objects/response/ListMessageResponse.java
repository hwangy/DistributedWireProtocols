package messenger.objects.response;

import java.util.List;
import java.util.stream.Collectors;

public class ListMessageResponse<T extends Object> implements MethodResponseInterface {
    private final Boolean success;
    private final List<T> message;

    public ListMessageResponse(Boolean success, List<T> message) {
        this.success = success;
        this.message = message;
    }

    public List<T> getMessages() {
        return message;
    }

    public Boolean isSuccessful() {
        return success;
    }
    @Override
    public String getStringStatus() {
        String response;
        if (success) {
            response = "Request processed successfully";
            if (message.size() > 0) {
                response += " with first message \"" + message.get(0) + "\"";
            }
        } else {
            response = "Request failed";
            if (message.size() > 0) {
                response += " with message \"" + message.get(0) + "\"";
            }
        }
        return response;
    }

    @Override
    public Response genGenericResponse() {
        return new Response(success, message.stream().map(
                Object::toString).collect(Collectors.toList()));
    }
}
