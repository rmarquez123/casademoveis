package used_furniture.restapi.posts.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostPhotoRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;
import used_furniture.core.products.repository.PhotoRepository;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.restapi.posts.client.PublicationResult;
import used_furniture.restapi.posts.client.SocialPublisher;

/*
 * Orchestrates the publication of posts to social platforms.
 */
public class PostPublicationService {

  private static final Logger LOG = LoggerFactory.getLogger(PostPublicationService.class);

  private final PostPublicationRepository publicationRepo;
  private final PostRepository postRepo;
  private final PostPhotoRepository postPhotoRepo;
  private final ProductRepository productRepo;
  private final PhotoRepository photoRepo;
  private final CaptionBuilderService captionBuilder;
  private final Map<SocialPlatform, SocialPublisher> publisherByPlatform;

  public PostPublicationService(PostPublicationRepository publicationRepo,
                                PostRepository postRepo,
                                PostPhotoRepository postPhotoRepo,
                                ProductRepository productRepo,
                                PhotoRepository photoRepo,
                                CaptionBuilderService captionBuilder,
                                Map<SocialPlatform, SocialPublisher> publisherByPlatform) {
    this.publicationRepo = publicationRepo;
    this.postRepo = postRepo;
    this.postPhotoRepo = postPhotoRepo;
    this.productRepo = productRepo;
    this.photoRepo = photoRepo;
    this.captionBuilder = captionBuilder;
    this.publisherByPlatform = publisherByPlatform;
  }

  /*
   * Process all due publications up to the given limit.
   */
  public void processDuePublications(int limit) {
    OffsetDateTime now = OffsetDateTime.now();
    List<PostPublication> due = publicationRepo.findDuePublications(now, limit);
    LOG.info("Found {} due publications to process", due.size());

    for (PostPublication pub : due) {
      try {
        publishSingle(pub);
      } catch (Exception ex) {
        LOG.error("Unexpected error while publishing postPublicationId={}",
            pub.getPostPublicationId(), ex);
        markAsFailed(pub, "Unexpected error: " + ex.getMessage());
      }
    }
  }

  /*
   * Publish a single PostPublication.
   */
  public void publishSingle(PostPublication pub) {

    if (pub.getStatus().isTerminal()) {
      LOG.info("Skipping publication id={} because status={} is terminal",
          pub.getPostPublicationId(), pub.getStatus());
      return;
    }

    Optional<Post> postOpt = postRepo.findById(pub.getPostId());
    if (postOpt.isEmpty()) {
      markAsFailed(pub, "Post not found id=" + pub.getPostId());
      return;
    }
    Post post = postOpt.get();

    Optional<Product> productOpt = productRepo.findById(post.getProductId());
    if (productOpt.isEmpty()) {
      markAsFailed(pub, "Product not found id=" + post.getProductId());
      return;
    }
    Product product = productOpt.get();

    List<PostPhoto> postPhotos = postPhotoRepo.findByPostId(post.getPostId());
    List<Photo> productPhotos = photoRepo.findByProductId(product.getProductId());

    // Resolve caption: first use override if present, otherwise use builder
    String caption = resolveCaption(pub, post, product);

    SocialPublisher publisher = publisherByPlatform.get(pub.getPlatform());
    if (publisher == null) {
      markAsFailed(pub, "No SocialPublisher registered for platform " + pub.getPlatform());
      return;
    }

    LOG.info("Publishing postPublicationId={} platform={} productId={}",
        pub.getPostPublicationId(), pub.getPlatform(), product.getProductId());

    pub.incrementAttemptCount();
    pub.setLastAttemptAt(OffsetDateTime.now());
    pub.setStatus(PublicationStatus.PUBLISHING);
    publicationRepo.update(pub);

    PublicationResult result = publisher.publish(post, product, postPhotos, productPhotos, caption);

    if (result.isSuccess()) {
      pub.setStatus(PublicationStatus.PUBLISHED);
      pub.setPublishedAt(OffsetDateTime.now());
      pub.setPlatformPostId(result.getPlatformPostId());
      pub.setErrorMessage(null);
      publicationRepo.update(pub);
      LOG.info("Successfully published postPublicationId={} platformPostId={}",
          pub.getPostPublicationId(), result.getPlatformPostId());
    } else {
      markAsFailed(pub, result.getErrorMessage());
    }
  }

  private String resolveCaption(PostPublication pub, Post post, Product product) {
    String override = pub.getCaptionOverride();
    if (override != null && !override.isBlank()) {
      return override;
    }
    return captionBuilder.buildCaption(post, product);
  }

  private void markAsFailed(PostPublication pub, String errorMessage) {
    pub.incrementAttemptCount();
    pub.setLastAttemptAt(OffsetDateTime.now());
    pub.setStatus(PublicationStatus.FAILED);
    pub.setErrorMessage(errorMessage);
    publicationRepo.update(pub);
    LOG.warn("Marked publication id={} as FAILED: {}",
        pub.getPostPublicationId(), errorMessage);
  }
}
