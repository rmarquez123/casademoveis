package used_furniture.restapi.ai_product_description;

/*
 * Interface for calling OpenAI to generate product descriptions.
 * This hides HTTP details from the rest of the code.
 */
public interface OpenAiProductDescriptionClient {

    /*
     * Sends the payload to OpenAI and returns a parsed result object.
     */
    OpenAiProductDescriptionResult generateProductDescription(
            OpenAiProductDescriptionPayload payload);
}
