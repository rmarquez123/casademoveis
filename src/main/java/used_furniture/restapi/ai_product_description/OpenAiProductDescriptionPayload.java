package used_furniture.restapi.ai_product_description;

import java.util.List;

/*
 * Payload passed from PromptBuilder to the OpenAI client.
 * It already contains:
 * - System prompt (role/context)
 * - User prompt (instructions + metadata)
 * - Raw image bytes to attach to the vision model call
 */
public class OpenAiProductDescriptionPayload {

    private String systemPrompt;
    private String userPrompt;
    private List<byte[]> imageBytes;

    public OpenAiProductDescriptionPayload(String systemPrompt,
                                           String userPrompt,
                                           List<byte[]> imageBytes) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.imageBytes = imageBytes;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public List<byte[]> getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(List<byte[]> imageBytes) {
        this.imageBytes = imageBytes;
    }
}
