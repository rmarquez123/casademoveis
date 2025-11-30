package used_furniture.restapi.posts.web;

import java.time.OffsetDateTime;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;

/*
 * Lightweight view of a PostPublication for admin/debugging.
 */
public class PublicationStatusResponse {

  private long postPublicationId;
  private long postId;
  private SocialPlatform platform;
  private PublicationStatus status;
  private OffsetDateTime scheduledTime;
  private OffsetDateTime publishedAt;
  private String platformPostId;
  private String errorMessage;

  public PublicationStatusResponse() {
  }

  public PublicationStatusResponse(long postPublicationId,
                                   long postId,
                                   SocialPlatform platform,
                                   PublicationStatus status,
                                   OffsetDateTime scheduledTime,
                                   OffsetDateTime publishedAt,
                                   String platformPostId,
                                   String errorMessage) {
    this.postPublicationId = postPublicationId;
    this.postId = postId;
    this.platform = platform;
    this.status = status;
    this.scheduledTime = scheduledTime;
    this.publishedAt = publishedAt;
    this.platformPostId = platformPostId;
    this.errorMessage = errorMessage;
  }

  public long getPostPublicationId() {
    return postPublicationId;
  }

  public long getPostId() {
    return postId;
  }

  public SocialPlatform getPlatform() {
    return platform;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public OffsetDateTime getScheduledTime() {
    return scheduledTime;
  }

  public OffsetDateTime getPublishedAt() {
    return publishedAt;
  }

  public String getPlatformPostId() {
    return platformPostId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
