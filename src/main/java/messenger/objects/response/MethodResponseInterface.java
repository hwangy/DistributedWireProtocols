package messenger.objects.response;

/**
 * The interface for all API-specific response objects.
 * All such objects should be able to return the identifier
 * of the API call they handle, as well as convert to
 * a generic Response object.
 */
public interface MethodResponseInterface {

    /**
     * Convert the request into a generic response, which
     * represents all responses as a list of strings.
     * @return The generic Response object.
     */
    public Response genGenericResponse();

    /**
     * Return the status for the API call correpsonding
     * to the response.
     * @return The String status.
     */
    public String getStringStatus();
}
