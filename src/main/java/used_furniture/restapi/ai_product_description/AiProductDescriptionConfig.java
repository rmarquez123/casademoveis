package used_furniture.restapi.ai_product_description;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/*
 * Spring configuration for the AI product description feature.
 * Reads settings from the Environment (e.g. properties file, env vars)
 * and wires the beans together.
 */
@Configuration
public class AiProductDescriptionConfig {

    private final Environment env;

    @Autowired
    public AiProductDescriptionConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public OpenAiModelSettings openAiModelSettings() {
        OpenAiModelSettings settings = new OpenAiModelSettings();

        settings.setApiKey(env.getProperty("ai.openai.apiKey"));
        settings.setApiUrl(env.getProperty(
                "ai.openai.apiUrl",
                "https://api.openai.com/v1/chat/completions"
        ));
        settings.setModelName(env.getProperty(
                "ai.openai.modelName",
                "gpt-4.1-mini"   // adjust as needed
        ));

        settings.setMaxTokens(env.getProperty("ai.openai.maxTokens", Integer.class, 512));
        settings.setTemperature(env.getProperty("ai.openai.temperature", Double.class, 0.6));
        settings.setTimeoutMillis(env.getProperty("ai.openai.timeoutMillis", Integer.class, 15000));

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
}
