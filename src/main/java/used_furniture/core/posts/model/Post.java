package used_furniture.core.posts.model;

import java.time.OffsetDateTime;

/*
 * Canonical social post for a product.
 * Maps to posts.post.
 */
public class Post {

  private long postId;
  private int productId;
  private String title;
  private String caption;
  private String languageCode;
  private OffsetDateTime createdAt;
  private OffsetDateTime desiredPublishTime;
  private boolean active;
  private String notes;

  /*
   * Empty constructor for frameworks and mappers.
   */
  public Post() {
  }

  /*
   * Convenience constructor for creating a new Post in code.
   * postId is left unset and should be filled after insert.
   */
  public Post(int productId,
              String title,
              String caption,
              String languageCode,
              OffsetDateTime desiredPublishTime,
              boolean active,
              String notes) {
    this.productId = productId;
    this.title = title;
    this.caption = caption;
    this.languageCode = languageCode;
    this.desiredPublishTime = desiredPublishTime;
    this.active = active;
    this.notes = notes;
  }

  /* Getters and setters */

  public long getPostId() {
    return postId;
  }

  public void setPostId(long postId) {
    this.postId = postId;
  }

  public int getProductId() {
    return productId;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getDesiredPublishTime() {
    return desiredPublishTime;
  }

  public void setDesiredPublishTime(OffsetDateTime desiredPublishTime) {
    this.desiredPublishTime = desiredPublishTime;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
