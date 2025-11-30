package used_furniture.core.posts.model;

import java.time.OffsetDateTime;

/*
 * Per-platform publication state for a Post.
 * Maps to posts.post_publication.
 */
public class PostPublication {

  private long postPublicationId;
  private long postId;
  private SocialPlatform platform;
  private String targetAccount;
  private String captionOverride;
  private PublicationStatus status;
  private OffsetDateTime scheduledTime;
  private OffsetDateTime publishedAt;
  private String platformPostId;
  private String errorMessage;
  private OffsetDateTime lastAttemptAt;
  private int attemptCount;

  public PostPublication() {
  }

  /*
   * Convenience constructor for creating a new publication.
   */
  public PostPublication(long postId,
                         SocialPlatform platform,
                         String targetAccount,
                         String captionOverride,
                         PublicationStatus status,
                         OffsetDateTime scheduledTime) {
    this.postId = postId;
    this.platform = platform;
    this.targetAccount = targetAccount;
    this.captionOverride = captionOverride;
    this.status = status;
    this.scheduledTime = scheduledTime;
  }

  public long getPostPublicationId() {
    return postPublicationId;
  }

  public void setPostPublicationId(long postPublicationId) {
    this.postPublicationId = postPublicationId;
  }

  public long getPostId() {
    return postId;
  }

  public void setPostId(long postId) {
    this.postId = postId;
  }

  public SocialPlatform getPlatform() {
    return platform;
  }

  public void setPlatform(SocialPlatform platform) {
    this.platform = platform;
  }

  public String getTargetAccount() {
    return targetAccount;
  }

  public void setTargetAccount(String targetAccount) {
    this.targetAccount = targetAccount;
  }

  public String getCaptionOverride() {
    return captionOverride;
  }

  public void setCaptionOverride(String captionOverride) {
    this.captionOverride = captionOverride;
  }

  public PublicationStatus getStatus() {
    return status;
  }

  public void setStatus(PublicationStatus status) {
    this.status = status;
  }

  public OffsetDateTime getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(OffsetDateTime scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public OffsetDateTime getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(OffsetDateTime publishedAt) {
    this.publishedAt = publishedAt;
  }

  public String getPlatformPostId() {
    return platformPostId;
  }

  public void setPlatformPostId(String platformPostId) {
    this.platformPostId = platformPostId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public OffsetDateTime getLastAttemptAt() {
    return lastAttemptAt;
  }

  public void setLastAttemptAt(OffsetDateTime lastAttemptAt) {
    this.lastAttemptAt = lastAttemptAt;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public void setAttemptCount(int attemptCount) {
    this.attemptCount = attemptCount;
  }

  /*
   * Helper to increment attempt count in a controlled way.
   */
  public void incrementAttemptCount() {
    this.attemptCount = this.attemptCount + 1;
  }
}
