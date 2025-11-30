package used_furniture.restapi.posts.client;

/*
 * Result of attempting to publish a post on a social platform.
 */
public class PublicationResult {

  private final boolean success;
  private final String platformPostId;
  private final String errorMessage;

  private PublicationResult(boolean success, String platformPostId, String errorMessage) {
    this.success = success;
    this.platformPostId = platformPostId;
    this.errorMessage = errorMessage;
  }

  public static PublicationResult success(String platformPostId) {
    return new PublicationResult(true, platformPostId, null);
  }

  public static PublicationResult failure(String errorMessage) {
    return new PublicationResult(false, null, errorMessage);
  }

  public boolean isSuccess() {
    return success;
  }

  public String getPlatformPostId() {
    return platformPostId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
