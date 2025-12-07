package used_furniture.core.posts.model;

/*
 * Lifecycle status for a post publication.
 * Mirrors the "status" column in posts.post_publication.
 */
public enum PublicationStatus {
  PENDING,
  QUEUED,
  PUBLISHING,
  PUBLISHED,
  FAILED,
  CANCELLED,
  SKIPPED;

  /*
   * Helper flag for scheduler logic. Terminal states are those
   * that don't require further automatic processing.
   */
  public boolean isTerminal() {
    return switch (this) {
      case PUBLISHED, FAILED, CANCELLED, SKIPPED -> true;
      default -> false;
    };
  }

  public boolean isPendingLike() {
    return switch (this) {
      case PENDING, QUEUED -> true;
      default -> false;
    };
  }
}
