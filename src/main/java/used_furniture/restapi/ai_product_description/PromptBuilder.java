package used_furniture.restapi.ai_product_description;

import java.util.List;

/*
 * Builds OpenAI payloads (system + user prompts) for the
 * product description feature.
 */
public class PromptBuilder {

  /*
     * Builds a payload for generating a product title and description
     * in Brazilian Portuguese, based on images and optional metadata.
   */
  public OpenAiProductDescriptionPayload buildProductDescriptionPayload(
          AiProductDescriptionRequest request,
          List<byte[]> imageBytes) {

    String systemPrompt = buildSystemPrompt();
    String userPrompt = buildUserPrompt(request);

    return new OpenAiProductDescriptionPayload(systemPrompt, userPrompt, imageBytes);
  }

  /*
     * System prompt describes the role and constraints of the model.
   */
  private String buildSystemPrompt() {
    StringBuilder sb = new StringBuilder();
    sb.append("Você é um assistente especializado em criar títulos e ");
    sb.append("descrições de anúncios de móveis usados para o mercado brasileiro.\n");
    sb.append("Regras importantes:\n");
    sb.append("- Escreva sempre em português do Brasil.\n");
    sb.append("- Foque em anúncios para sites de venda e Facebook Marketplace.\n");
    sb.append("- Use linguagem clara, amigável e objetiva.\n");
    sb.append("- Nunca invente informações que não estejam visíveis nas imagens ou ");
    sb.append("nos dados fornecidos (marca, medidas exatas, defeitos invisíveis, etc.).\n");
    sb.append("- Se algo não estiver claro, seja neutro (por exemplo: ");
    sb.append("\"não foi possível identificar a marca pelas fotos\").\n");
    sb.append("- Para inferências como categoria, condição ou cômodo, sempre inclua ");
    sb.append("um nível de confiança: baixa, média ou alta.\n");
    sb.append("- Responda estritamente em JSON válido, sem texto extra.\n");
    sb.append("Formato JSON esperado:\n");
    sb.append("{\n");
    sb.append("  \"title_pt\": \"...\",\n");
    sb.append("  \"description_pt\": \"...\",\n");
    sb.append("  \"title_en\": null,\n");
    sb.append("  \"description_en\": null,\n");
    sb.append("  \"tags\": [\"tag1\", \"tag2\"],\n");
    sb.append("  \"category\": {\n");
    sb.append("    \"value\": \"sofa|mesa|cadeira|armario|geladeira|fogao|outro\",\n");
    sb.append("    \"confidence\": \"baixa|média|alta\"\n");
    sb.append("  },\n");
    sb.append("  \"condition\": {\n");
    sb.append("    \"value\": \"novo|excelente|bom|regular|precisa_reparo\",\n");
    sb.append("    \"confidence\": \"baixa|média|alta\"\n");
    sb.append("  },\n");
    sb.append("  \"room_type\": {\n");
    sb.append("    \"value\": \"sala|quarto|cozinha|escritorio|varanda|outro\",\n");
    sb.append("    \"confidence\": \"baixa|média|alta\"\n");
    sb.append("  },\n");
    sb.append("  \"confidence_notes\": \"breve explicação sobre o que não foi possível ver bem nas fotos\"\n");
    sb.append("}\n");
    return sb.toString();
  }

  /*
     * User prompt includes the concrete item metadata.
     * Images are passed separately via the imageBytes list.
   */
  private String buildUserPrompt(AiProductDescriptionRequest request) {
    StringBuilder sb = new StringBuilder();

    sb.append("Gere um título curto (máx. ~80 caracteres) e uma descrição atraente ");
    sb.append("para um anúncio de móvel usado com base nas imagens fornecidas.\n");

    if (request.getCategory() != null && !request.getCategory().isEmpty()) {
      sb.append("Categoria informada pelo usuário: ").append(request.getCategory()).append(".\n");
    } else {
      sb.append("Tente inferir uma categoria aproximada a partir das imagens.\n");
    }

    if (request.getLocation() != null && !request.getLocation().isEmpty()) {
      sb.append("Localização (bairro/cidade): ").append(request.getLocation()).append(".\n");
    }

    if (request.getPrice() != null && !request.getPrice().isEmpty()) {
      sb.append("Preço sugerido pelo usuário: ").append(request.getPrice()).append(".\n");
    }

    if (request.getDimensions() != null && !request.getDimensions().isEmpty()) {
      sb.append("Dimensões informadas: ").append(request.getDimensions()).append(".\n");
    } else {
      sb.append("Não invente medidas exatas se não forem informadas.\n");
    }

    if (request.getConditionHint() != null && !request.getConditionHint().isEmpty()) {
      sb.append("Condição informada: ").append(request.getConditionHint()).append(".\n");
    }

    sb.append("Inclua detalhes úteis como tipo de material, estilo e uso ideal, ");
    sb.append("apenas se forem claramente percebidos nas fotos.\n");
    sb.append("Lembre-se de responder apenas com o JSON no formato especificado.\n");

    return sb.toString();
  }
}
