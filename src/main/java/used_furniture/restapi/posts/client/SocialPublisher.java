package used_furniture.restapi.posts.client;

import java.util.List;
import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;

/*
 * Platform-specific publisher (Instagram, Facebook, etc).
 * Called by PostPublicationService, not by controllers directly.
 */
public interface SocialPublisher {

  SocialPlatform getPlatform();

  /*
   * Publish the given post/product/photos to the external platform.
   *
   * The "caption" argument is already resolved (post caption or override).
   */
  PublicationResult publish(Post post,
                            Product product,
                            List<PostPhoto> postPhotos,
                            List<Photo> productPhotos,
                            String caption);
  
  
  boolean deletePost(String platformPostId); 
}
