package used_furniture.restapi.posts.client;

/*
 * Minimal mapping of Facebook's response for /photos or /feed.
 * We only care about the "id" field.
 */
public class FacebookPostResponse {
  private String id;

  public FacebookPostResponse() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
