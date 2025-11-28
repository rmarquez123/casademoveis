package used_furniture.restapi.ai_product_description;

/*
 * Confidence level for inferred fields returned by the LLM.
 */
public enum InferenceConfidence {
    LOW,
    MEDIUM,
    HIGH;

    /*
     * Parses a textual confidence from the LLM into the enum.
     * Defaults to LOW when the value is not recognized.
     */
    public static InferenceConfidence fromText(String text) {
        if (text == null) {
            return LOW;
        }
        String normalized = text.trim().toLowerCase();
        if ("medium".equals(normalized) || "médio".equals(normalized)) {
            return MEDIUM;
        }
        if ("high".equals(normalized) || "alta".equals(normalized)) {
            return HIGH;
        }
        if ("low".equals(normalized) || "baixa".equals(normalized)) {
            return LOW;
        }
        return LOW;
    }
}
