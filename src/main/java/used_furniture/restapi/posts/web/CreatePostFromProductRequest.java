package used_furniture.restapi.posts.web;

/*
 * Request body for creating/ensuring a Post from a product.
 */
public class CreatePostFromProductRequest {

  private int productId;

  public CreatePostFromProductRequest() {
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }
}
