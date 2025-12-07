package used_furniture.core.posts.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;

/*
 * Data access contract for posts.post_publication.
 */
public interface PostPublicationRepository {

  /*
   * Load publication by primary key.
   */
  Optional<PostPublication> findById(long postPublicationId);

  /*
   * Load publication for a given post and platform, if it exists.
   */
  Optional<PostPublication> findByPostAndPlatform(long postId, SocialPlatform platform);

  /*
   * Insert a new PostPublication and return it with postPublicationId populated.
   */
  PostPublication insert(PostPublication publication);

  /*
   * Update an existing publication.
   */
  void update(PostPublication publication);

  /*
   * Find publications that are due to be processed by the scheduler.
   * Implementations should typically select:
   *   status IN (PENDING, QUEUED)
   *   AND (scheduled_time IS NULL OR scheduled_time <= now)
   * ordered by scheduled_time and id, limited by "limit".
   *
   * The "now" parameter is passed from the caller to keep logic testable.
   */
  List<PostPublication> findDuePublications(OffsetDateTime now, int limit);

  /*
   * Optionally, a helper to list by status for admin screens.
   */
  List<PostPublication> findByStatus(PublicationStatus status, int limit);
  
  
  List<PostPublication> findByPost(long postId);


}
