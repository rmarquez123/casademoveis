package used_furniture.restapi.posts.service;

import used_furniture.core.posts.model.Post;
import used_furniture.core.products.model.Product;

public class CaptionBuilderService {

  /*
   * Build a caption for the post.
   * If the post already has a caption, return it.
   * Else generate from product metadata.
   */
  public String buildCaption(Post post, Product product) {

    if (post.getCaption() != null && !post.getCaption().isBlank()) {
      return post.getCaption();
    }

    StringBuilder sb = new StringBuilder();

    // Title
    sb.append(product.getName()).append("\n\n");

    // Optional description
    if (product.getDescription() != null && !product.getDescription().isBlank()) {
      sb.append(product.getDescription()).append("\n\n");
    }

    // Price
    if (product.getPrice() != 0) {
      sb.append("Preço: R$ ")
        .append(String.format("%.2f", product.getPrice()))
        .append("\n");
    }

    // Location is currently implicit: Vila Mariana
    sb.append("Local: Vila Mariana, São Paulo\n\n");

    // Call to action
    sb.append("Interessado? Chama no WhatsApp!\n\n");

    // Basic hashtags — we can expand later by category
    sb.append("#casademoveisusados #moveisusados #vilamariana");

    return sb.toString().trim();
  }
}
