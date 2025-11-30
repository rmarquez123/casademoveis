package used_furniture.core.posts.model;

/*
 * Association between a canonical Post and a Product photo.
 * Supports carousels and a single primary image.
 * Maps to posts.post_photo.
 */
public class PostPhoto {

  private long postPhotoId;
  private long postId;
  private long photoId;
  private int sortOrder;
  private boolean primary;

  public PostPhoto() {
  }

  public PostPhoto(long postId, long photoId, int sortOrder, boolean primary) {
    this.postId = postId;
    this.photoId = photoId;
    this.sortOrder = sortOrder;
    this.primary = primary;
  }

  public long getPostPhotoId() {
    return postPhotoId;
  }

  public void setPostPhotoId(long postPhotoId) {
    this.postPhotoId = postPhotoId;
  }

  public long getPostId() {
    return postId;
  }

  public void setPostId(long postId) {
    this.postId = postId;
  }

  public long getPhotoId() {
    return photoId;
  }

  public void setPhotoId(long photoId) {
    this.photoId = photoId;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }
}
