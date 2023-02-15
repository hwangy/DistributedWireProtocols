package messenger.objects.request;

/**
 * The interface for all API-specific request objects.
 * All such objects should be able to return the identifier
 * of the API call they handle, as well as convert to
 * a generic Request object.
 */
public interface MethodRequestInterface {
    /**
     * Return the identifier for the API call corresponding
     * to the request.
     * @return  The integer identifier.
     */
    int getIdentifier();

    /**
     * Convert the request into a generic request, which
     * represents all arguments as a list of strings.
     * @return  The generic Request object.
     */
    Request genGenericRequest();
}
