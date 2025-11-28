package used_furniture.restapi.ai_product_description;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiProductDescriptionConfig {

  private final Properties aiProps;

  @Autowired
  public AiProductDescriptionConfig(@Qualifier("aiProps") Properties aiProps) {
    this.aiProps = aiProps;
  }

  @Bean
  public OpenAiModelSettings openAiModelSettings() {
    // REQUIRED: API key
    String apiKey = trimToNull(aiProps.getProperty("ai.openai.apiKey"));
    if (apiKey == null) {
      StringBuilder sb = new StringBuilder();
      sb.append("Missing required property 'ai.openai.apiKey'.\n")
        .append("Make sure it is defined in ai-product-description.properties\n")
        .append("  (loaded via <util:properties id=\"aiProps\" ... />)\n");
      throw new IllegalStateException(sb.toString());
    }

    // OPTIONALS WITH DEFAULTS
    String apiUrl = defaultIfBlank(
        aiProps.getProperty("ai.openai.apiUrl"),
        "https://api.openai.com/v1/chat/completions"
    );

    String modelName = defaultIfBlank(
        aiProps.getProperty("ai.openai.modelName"),
        "gpt-4.1-mini"
    );

    int maxTokens = parseIntOrDefault(
        aiProps.getProperty("ai.openai.maxTokens"),
        512
    );

    double temperature = parseDoubleOrDefault(
        aiProps.getProperty("ai.openai.temperature"),
        0.6
    );

    int timeoutMillis = parseIntOrDefault(
        aiProps.getProperty("ai.openai.timeoutMillis"),
        15000
    );

    System.out.println("[AiProductDescriptionConfig] OpenAI settings resolved:");
    System.out.println("  apiUrl      = " + apiUrl);
    System.out.println("  modelName   = " + modelName);
    System.out.println("  maxTokens   = " + maxTokens);
    System.out.println("  temperature = " + temperature);
    System.out.println("  timeoutMs   = " + timeoutMillis);

    OpenAiModelSettings settings = new OpenAiModelSettings();
    settings.setApiKey(apiKey);
    settings.setApiUrl(apiUrl);
    settings.setModelName(modelName);
    settings.setMaxTokens(maxTokens);
    settings.setTemperature(temperature);
    settings.setTimeoutMillis(timeoutMillis);

    return settings;
  }

  @Bean
  public PromptBuilder promptBuilder() {
    return new PromptBuilder();
  }

  @Bean
  public OpenAiProductDescriptionClient openAiProductDescriptionClient(
          OpenAiModelSettings settings) {
    return new OpenAiProductDescriptionClientImpl(settings);
  }

  @Bean
  public AiProductDescriptionService aiProductDescriptionService(
          OpenAiProductDescriptionClient client,
          PromptBuilder promptBuilder) {
    return new AiProductDescriptionService(client, promptBuilder);
  }

  // --- helpers ---

  private String trimToNull(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String defaultIfBlank(String value, String defaultVal) {
    String trimmed = trimToNull(value);
    return trimmed != null ? trimmed : defaultVal;
  }

  private int parseIntOrDefault(String value, int defaultVal) {
    try {
      return value != null ? Integer.parseInt(value.trim()) : defaultVal;
    } catch (NumberFormatException ex) {
      return defaultVal;
    }
  }

  private double parseDoubleOrDefault(String value, double defaultVal) {
    try {
      return value != null ? Double.parseDouble(value.trim()) : defaultVal;
    } catch (NumberFormatException ex) {
      return defaultVal;
    }
  }
}
