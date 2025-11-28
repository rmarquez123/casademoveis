package used_furniture.restapi.ai_product_description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/*
 * Service responsible for orchestrating the AI-based product description flow.
 * It validates input, converts images, calls the OpenAI client, and applies
 * the "medium or higher" confidence rule for inferred fields.
 */
public class AiProductDescriptionService {

  private final OpenAiProductDescriptionClient openAiClient;
  private final PromptBuilder promptBuilder;

  public AiProductDescriptionService(OpenAiProductDescriptionClient openAiClient,
          PromptBuilder promptBuilder) {
    this.openAiClient = openAiClient;
    this.promptBuilder = promptBuilder;
  }

  /*
     * Main entry point used by the controller.
     * images: uploaded images from the request (must contain at least one item).
   */
  public AiProductDescriptionResponse generateDescription(
          AiProductDescriptionRequest request,
          List<MultipartFile> images) {

    validateInput(images);

    List<byte[]> imageBytes = toImageBytes(images);

    OpenAiProductDescriptionPayload payload
            = promptBuilder.buildProductDescriptionPayload(request, imageBytes);

    OpenAiProductDescriptionResult result
            = openAiClient.generateProductDescription(payload);

    return mapResultToResponse(result);
  }

  /*
     * Validates that the request contains at least one image.
     * Additional constraints (max count, size, content type) can be added here.
   */
  private void validateInput(List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      throw new IllegalArgumentException("At least one image is required.");
    }
  }

  /*
     * Converts MultipartFile instances to raw byte arrays.
     * This keeps image handling inside the service instead of the controller.
   */
  private List<byte[]> toImageBytes(List<MultipartFile> images) {
    List<byte[]> bytesList = new ArrayList<>();
    for (MultipartFile file : images) {
      if (file == null || file.isEmpty()) {
        continue;
      }
      try {
        bytesList.add(file.getBytes());
      } catch (IOException ex) {
        /*
                 * For now, propagate as a runtime exception.
                 * This can be replaced with a custom exception type if desired.
         */
        throw new RuntimeException("Failed to read uploaded image.", ex);
      }
    }
    if (bytesList.isEmpty()) {
      throw new IllegalArgumentException("No valid image data provided.");
    }
    return bytesList;
  }

  /*
     * Applies the "only if confidence is medium or higher" rule when mapping
     * the internal OpenAI result to the public response DTO.
   */
  private AiProductDescriptionResponse mapResultToResponse(
          OpenAiProductDescriptionResult result) {

    AiProductDescriptionResponse response = new AiProductDescriptionResponse();

    response.setTitlePt(result.getTitlePt());
    response.setDescriptionPt(result.getDescriptionPt());

    /* Optional future fields */
    response.setTitleEn(result.getTitleEn());
    response.setDescriptionEn(result.getDescriptionEn());

    response.setTags(result.getTags());

    /* Apply the confidence rule for inferred categorical fields */
    if (result.getCategory() != null && result.getCategory().isAtLeastMedium()) {
      response.setCategory(result.getCategory().getValue());
    }

    if (result.getCondition() != null && result.getCondition().isAtLeastMedium()) {
      response.setCondition(result.getCondition().getValue());
    }

    if (result.getRoomType() != null && result.getRoomType().isAtLeastMedium()) {
      response.setRoomType(result.getRoomType().getValue());
    }

    response.setConfidenceNotes(result.getConfidenceNotes());

    return response;
  }
}
