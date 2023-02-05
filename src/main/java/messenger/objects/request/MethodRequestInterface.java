package messenger.objects.request;

import messenger.objects.response.MethodResponseInterface;

public interface MethodRequestInterface {
    public int getIdentifier();
    public Request genGenericRequest();
}
