package used_furniture.restapi.ai_product_description;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

/*
 * Basic skeleton implementation for calling OpenAI's chat/vision endpoint.
 * The exact JSON payload structure may need adjustment depending on
 * the OpenAI API version and client library you use.
 */
public class OpenAiProductDescriptionClientImpl implements OpenAiProductDescriptionClient {

  private final OpenAiModelSettings settings;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public OpenAiProductDescriptionClientImpl(OpenAiModelSettings settings) {
    this.settings = settings;
    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(settings.getTimeoutMillis()))
            .build();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public OpenAiProductDescriptionResult generateProductDescription(
          OpenAiProductDescriptionPayload payload) {

    String requestBodyJson = buildRequestBodyJson(payload);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(settings.getApiUrl())) // e.g. https://api.openai.com/v1/chat/completions
            .timeout(Duration.ofMillis(settings.getTimeoutMillis()))
            .header("Authorization", "Bearer " + settings.getApiKey())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
            .build();

    try {
      HttpResponse<String> response = httpClient.send(
              request,
              HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
      );

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        String responseBody = response.body();
        return parseResponseJson(responseBody);
      } else {
        throw new RuntimeException("OpenAI error: HTTP " + response.statusCode()
                + " - " + response.body());
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to call OpenAI API", e);
    }
  }

  /*
     * Builds the JSON request body for a chat-completion with images.
     * Adjust this according to the specific OpenAI API version and model.
   */
 /*
 * Builds the JSON request body for a chat-completion with image input.
 * Images are encoded as data URLs and sent via "image_url".
   */
  private String buildRequestBodyJson(OpenAiProductDescriptionPayload payload) {
    StringBuilder sb = new StringBuilder();

    sb.append("{");
    sb.append("\"model\": ").append(jsonString(settings.getModelName())).append(",");
    sb.append("\"temperature\": ").append(settings.getTemperature()).append(",");
    sb.append("\"max_tokens\": ").append(settings.getMaxTokens()).append(",");
    sb.append("\"messages\": [");

    // System message
    sb.append("{");
    sb.append("\"role\": \"system\",");
    sb.append("\"content\": [");
    sb.append("{\"type\": \"text\", \"text\": ").append(jsonString(payload.getSystemPrompt())).append("}");
    sb.append("]");
    sb.append("},");

    // User message with text + images
    sb.append("{");
    sb.append("\"role\": \"user\",");
    sb.append("\"content\": [");

    // Text part
    sb.append("{\"type\": \"text\", \"text\": ").append(jsonString(payload.getUserPrompt())).append("}");

    // Image parts (as data URLs)
    List<byte[]> images = payload.getImageBytes();
    if (images != null && !images.isEmpty()) {
      for (byte[] img : images) {
        String base64 = Base64.getEncoder().encodeToString(img);
        String dataUrl = "data:image/jpeg;base64," + base64;

        sb.append(",{");
        sb.append("\"type\": \"image_url\",");
        sb.append("\"image_url\": {");
        sb.append("\"url\": ").append(jsonString(dataUrl));
        // optionally: sb.append(",\"detail\": \"low\"");
        sb.append("}");
        sb.append("}");
      }
    }

    sb.append("]");
    sb.append("}"); // end user message

    sb.append("]}"); // end root

    return sb.toString();
  }


  /*
 * Extracts the assistant content as a string.
 * Supports:
 * - legacy: message.content is a plain string
 * - multimodal: message.content is an array [{type: "text", text: "..."}]
   */
  private String extractAssistantContentAsString(JsonNode messageNode) {
    JsonNode contentNode = messageNode.path("content");

    // Case 1: content is a simple string
    if (contentNode.isTextual()) {
      return contentNode.asText();
    }

    // Case 2: content is an array of parts (multimodal)
    if (contentNode.isArray()) {
      for (JsonNode part : contentNode) {
        String type = part.path("type").asText("");
        if ("text".equals(type)) {
          JsonNode textNode = part.path("text");
          if (textNode.isTextual()) {
            return textNode.asText();
          }
        }
      }
    }

    throw new RuntimeException("Could not find text content in assistant message");
  }

  /*
 * Parses an inferred field object:
 * { "value": "sofa", "confidence": "média" }
   */
  private InferredField<String> parseInferredField(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }

    String value = getTextOrNull(node, "value");
    String confText = getTextOrNull(node, "confidence");

    InferenceConfidence conf = InferenceConfidence.fromText(confText);
    return new InferredField<>(value, conf);
  }

  private String getTextOrNull(JsonNode node, String fieldName) {
    JsonNode f = node.path(fieldName);
    return f.isMissingNode() || f.isNull() ? null : f.asText();
  }


  /*
 * Parses the JSON returned by OpenAI into OpenAiProductDescriptionResult.
 * Expects the assistant message to contain a single text content block
 * with a JSON object matching the schema defined in PromptBuilder.
   */
 /*
 * Parses the JSON returned by OpenAI into OpenAiProductDescriptionResult.
 * Expects the assistant message to contain a JSON object, possibly wrapped
 * in Markdown code fences (``` or ```json).
   */
  private OpenAiProductDescriptionResult parseResponseJson(String json) {
    try {
      JsonNode root = objectMapper.readTree(json);

      // 1) Get choices[0].message
      JsonNode choices = root.path("choices");
      if (!choices.isArray() || choices.size() == 0) {
        throw new RuntimeException("OpenAI response missing choices");
      }

      JsonNode message = choices.get(0).path("message");
      if (message.isMissingNode()) {
        throw new RuntimeException("OpenAI response missing message");
      }

      // 2) Extract the assistant content (may be string or array of {type,text})
      String assistantContent = extractAssistantContentAsString(message);

      // 3) Remove Markdown fences if present
      String assistantContentJson = sanitizeAssistantJson(assistantContent);

      // 4) Now parse the assistant JSON string (the product description object)
      JsonNode obj = objectMapper.readTree(assistantContentJson);

      OpenAiProductDescriptionResult result = new OpenAiProductDescriptionResult();

      result.setTitlePt(getTextOrNull(obj, "title_pt"));
      result.setDescriptionPt(getTextOrNull(obj, "description_pt"));
      result.setTitleEn(getTextOrNull(obj, "title_en"));
      result.setDescriptionEn(getTextOrNull(obj, "description_en"));

      // tags
      JsonNode tagsNode = obj.path("tags");
      if (tagsNode.isArray()) {
        result.setTags(objectMapper.convertValue(
                tagsNode,
                objectMapper.getTypeFactory().constructCollectionType(
                        java.util.List.class, String.class)));
      }

      // category / condition / room_type with confidence
      result.setCategory(parseInferredField(obj.path("category")));
      result.setCondition(parseInferredField(obj.path("condition")));
      result.setRoomType(parseInferredField(obj.path("room_type")));

      result.setConfidenceNotes(getTextOrNull(obj, "confidence_notes"));

      return result;

    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI response JSON", e);
    }
  }

  /**
   * Removes Markdown code fences from the assistant's content if present.
   * Handles patterns like:
   *
   * ```json { ... } ```
   *
   * or
   *
   * ``` { ... } ```
   */
  private String sanitizeAssistantJson(String raw) {
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();

    // If it doesn't start with ``` just return as-is
    if (!trimmed.startsWith("```")) {
      return trimmed;
    }

    // Strip the opening fence: ``` or ```json (or ``` json)
    int firstNewline = trimmed.indexOf('\n');
    if (firstNewline > 0) {
      // Remove the first line (fence + optional language)
      trimmed = trimmed.substring(firstNewline + 1);
    }

    // Strip the closing fence: last occurrence of ```
    int lastFence = trimmed.lastIndexOf("```");
    if (lastFence >= 0) {
      trimmed = trimmed.substring(0, lastFence);
    }

    return trimmed.trim();
  }

  /*
     * Escapes a string for safe inclusion in JSON (very minimal implementation).
     * Replace with proper JSON builder in production code.
   */
  private String jsonString(String value) {
    if (value == null) {
      return "null";
    }
    String escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    return "\"" + escaped + "\"";
  }
}
