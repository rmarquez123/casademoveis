package used_furniture.restapi.posts.web;

import java.time.OffsetDateTime;
import used_furniture.core.posts.model.SocialPlatform;

/*
 * Request body for scheduling a publication to a specific platform.
 */
public class SchedulePublicationRequest {

  private long postId;
  private SocialPlatform platform;
  private String targetAccount;          // e.g. "Casa de Móveis Usados"
  private OffsetDateTime scheduledTime;  // optional, defaults to now if null
  private String captionOverride;        // optional

  public SchedulePublicationRequest() {
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

  public OffsetDateTime getScheduledTime() {
    return scheduledTime;
  }

  public void setScheduledTime(OffsetDateTime scheduledTime) {
    this.scheduledTime = scheduledTime;
  }

  public String getCaptionOverride() {
    return captionOverride;
  }

  public void setCaptionOverride(String captionOverride) {
    this.captionOverride = captionOverride;
  }
}
