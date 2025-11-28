package used_furniture.restapi.ai_product_description;

/*
 * Wrapper for a value inferred by the LLM with an associated confidence.
 */
public class InferredField<T> {

    private T value;
    private InferenceConfidence confidence;

    public InferredField() {
    }

    public InferredField(T value, InferenceConfidence confidence) {
        this.value = value;
        this.confidence = confidence;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public InferenceConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(InferenceConfidence confidence) {
        this.confidence = confidence;
    }

    /*
     * Convenience method to check if the value should be exposed
     * according to the "medium or higher" rule.
     */
    public boolean isAtLeastMedium() {
        return confidence == InferenceConfidence.MEDIUM
                || confidence == InferenceConfidence.HIGH;
    }
}
