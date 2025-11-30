package used_furniture.core.posts.repository;

import java.util.List;
import java.util.Optional;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.model.PostPhoto;

/*
 * Data access contract for posts.post_photo.
 */
public interface PostPhotoRepository {

  /*
   * Load all photos attached to a post, usually ordered by sortOrder.
   */
  List<PostPhoto> findByPostId(long postId);

  /*
   * Load the primary photo for a post, if any.
   */
  Optional<PostPhoto> findPrimaryByPostId(long postId);

  /*
   * Insert a new PostPhoto and return it with postPhotoId populated.
   */
  PostPhoto insert(PostPhoto postPhoto);

  /*
   * Delete all photo associations for a post.
   */
  void deleteByPostId(long postId);
}
