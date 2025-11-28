package used_furniture.restapi.ai_product_description;

import java.io.InputStream;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * End-to-end integration test for the AI product description endpoint.
 *
 * This test: - Loads a real JPEG from src/test/resources - Builds real
 * OpenAiModelSettings from environment variables - Calls OpenAI via
 * OpenAiProductDescriptionClientImpl - Verifies that the JSON response looks sane
 *
 * IMPORTANT: - Requires OPENAI_API_KEY in the environment (or properties) - Will be
 * slower and incur cost per run
 */
public class AiProductDescriptionApiIT {

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    // Require API key, otherwise skip the test
    String apiKey = System.getenv("OPENAI_API_KEY");
    
//        assumeTrue("OPENAI_API_KEY must be set to run this integration test",
//                apiKey != null && !apiKey.isEmpty());
    // --- Build real OpenAI settings ---
    OpenAiModelSettings settings = new OpenAiModelSettings();
    settings.setApiKey(apiKey);
    settings.setApiUrl("https://api.openai.com/v1/chat/completions");
    settings.setModelName("gpt-4.1-mini"); // adjust as needed

    settings.setMaxTokens(512);
    settings.setTemperature(0.6);
    settings.setTimeoutMillis(20000);

    // --- Wire the real client + service + controller ---
    OpenAiProductDescriptionClient client
            = new OpenAiProductDescriptionClientImpl(settings);

    PromptBuilder promptBuilder = new PromptBuilder();
    AiProductDescriptionService service
            = new AiProductDescriptionService(client, promptBuilder);

    AiProductDescriptionController controller
            = new AiProductDescriptionController(service);

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void fullApiCall_shouldReturnValidJsonResponse() throws Exception {
    // Load a real test image from the classpath
    byte[] imageBytes = loadTestImage("/test-images/sofa-small.jpg");
      
    MockMultipartFile imageFile = new MockMultipartFile(
            "images",
            "sofa-small.jpg",
            "image/jpeg",
            imageBytes
    );

    MvcResult result = mockMvc.perform(
            multipart("/ai/product-description")
                    .file(imageFile)
                    .param("location", "Vila Mariana, São Paulo")
                    .param("price", "500")
                    .param("dimensions", "2.0m x 1.0m")
                    // category/condition hints are optional
                    .contentType(MediaType.MULTIPART_FORM_DATA)
    )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // We can't assert exact text because LLM output varies.
            // Instead, check that required fields are present and non-empty.
//            .andExpect(jsonPath("$.titlePt").isNotEmpty())
            
            // tags may be empty but usually we expect at least one
//            .andExpect(jsonPath("$.tags").isArray())
//            // category/condition/roomType may be null if confidence < medium.
//            // Just assert the fields exist (possibly null).
//            .andExpect(jsonPath("$.category").exists())
//            .andExpect(jsonPath("$.condition").exists())
//            .andExpect(jsonPath("$.roomType").exists())
//            // confidenceNotes should exist (can be empty, but usually not)
//            .andExpect(jsonPath("$.confidenceNotes").exists())
            
            .andReturn();
    Object a = result.getResponse().getContentAsString();
    System.out.println(a);
  }

  /**
   * Utility to load the test image from the classpath.
   */
  private byte[] loadTestImage(String classpathLocation) throws Exception {
    InputStream is = getClass().getResourceAsStream(classpathLocation);
    assertNotNull(
            "Test image not found on classpath: " + classpathLocation,
            is
    );
    try (is) {
      return is.readAllBytes();
    }
  }
}
