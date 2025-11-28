package used_furniture.restapi.ai_product_description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AiProductDescriptionConfig {

  private final Environment env;

  @Autowired
  public AiProductDescriptionConfig(Environment env) {
    this.env = env;
  }

  @Bean
  public OpenAiModelSettings openAiModelSettings() {
    // ---- REQUIRED: API KEY ----
    String apiKey = trimToNull(env.getProperty("ai.openai.apiKey"));
    if (apiKey == null) {
      // Helpful error so you see it immediately at startup
      StringBuilder sb = new StringBuilder();
      sb.append("Missing required property 'ai.openai.apiKey'.\n")
        .append("Make sure it is defined in one of:\n")
        .append("  - ai-product-description.properties on the classpath\n")
        .append("  - application.properties (if you use it)\n")
        .append("  - environment variable / system property (ai.openai.apiKey)\n");
      throw new IllegalStateException(sb.toString());
    }

    // ---- OPTIONAL / WITH DEFAULTS ----
    String apiUrl = defaultIfBlank(
        env.getProperty("ai.openai.apiUrl"),
        "https://api.openai.com/v1/chat/completions"
    );

    String modelName = defaultIfBlank(
        env.getProperty("ai.openai.modelName"),
        "gpt-4.1-mini"
    );

    int maxTokens = env.getProperty("ai.openai.maxTokens", Integer.class, 512);
    double temperature = env.getProperty("ai.openai.temperature", Double.class, 0.6);
    int timeoutMillis = env.getProperty("ai.openai.timeoutMillis", Integer.class, 15000);

    // Optional: quick debug print so you can see what's coming from env
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

  // ---- helpers ----

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String defaultIfBlank(String value, String defaultVal) {
    String trimmed = trimToNull(value);
    return trimmed != null ? trimmed : defaultVal;
  }
}
