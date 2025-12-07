package used_furniture.restapi.posts.client;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;

/*
 * Fake publisher that just logs what would be posted.
 * Useful for initial wiring and tests.
 */
public class FakeSocialPublisher implements SocialPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(FakeSocialPublisher.class);

  private final SocialPlatform platform;

  public FakeSocialPublisher(SocialPlatform platform) {
    this.platform = platform;
  }

  @Override
  public SocialPlatform getPlatform() {
    return platform;
  }

  @Override
  public PublicationResult publish(Post post,
                                   Product product,
                                   List<PostPhoto> postPhotos,
                                   List<Photo> productPhotos,
                                   String caption) {
    LOG.info("FAKE PUBLISH [{}]: postId={}, productId={}, name={}",
        platform, post.getPostId(), product.getProductId(), product.getName());
    LOG.info("Caption:\n{}", caption);
    LOG.info("Attached photos: {} (postPhotos), {} (productPhotos)",
        postPhotos.size(), productPhotos.size());

    // Return a fake platform post id
    String fakeId = platform.name() + "_POST_" + post.getPostId();
    return PublicationResult.success(fakeId);
  }

  @Override
  public boolean deletePost(String platformPostId) {
    return true;
  }
  
  
}
