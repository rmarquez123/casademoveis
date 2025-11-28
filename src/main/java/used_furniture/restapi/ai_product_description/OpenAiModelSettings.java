package used_furniture.restapi.ai_product_description;

/*
 * Configuration holder for OpenAI model and HTTP settings.
 * Values should be injected from properties or environment variables.
 */
public class OpenAiModelSettings {

  private String apiKey;
  private String apiUrl;     // e.g. https://api.openai.com/v1/chat/completions
  private String modelName;  // e.g. gpt-4.1-mini or similar

  private int maxTokens = 512;
  private double temperature = 0.6;
  private int timeoutMillis = 15000;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public int getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(int maxTokens) {
    this.maxTokens = maxTokens;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public int getTimeoutMillis() {
    return timeoutMillis;
  }

  public void setTimeoutMillis(int timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
}
