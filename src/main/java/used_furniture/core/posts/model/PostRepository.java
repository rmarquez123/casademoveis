package used_furniture.core.posts.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import used_furniture.core.posts.model.Post;

/*
 * Data access contract for posts.post.
 */
public interface PostRepository {

  /*
   * Load a post by primary key.
   */
  Optional<Post> findById(long postId);

  /*
   * Load all posts for a given product.
   */
  List<Post> findByProductId(int productId);

  /*
   * Insert a new Post and return it with postId populated.
   */
  Post insert(Post post);

  /*
   * Update an existing Post. Implementations may throw if postId is missing.
   */
  void update(Post post);

  Optional<Post> findActiveByProductId(int productId);

  void deactivate(long postId);

}
