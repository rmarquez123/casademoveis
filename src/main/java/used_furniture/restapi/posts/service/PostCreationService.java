package used_furniture.restapi.posts.service;

import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.posts.repository.PostPhotoRepository;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.core.products.repository.PhotoRepository;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class PostCreationService {

  private final PostRepository postRepo;
  private final PostPhotoRepository postPhotoRepo;
  private final ProductRepository productRepo;
  private final PhotoRepository photoRepo;

  public PostCreationService(PostRepository postRepo,
          PostPhotoRepository postPhotoRepo,
          ProductRepository productRepo,
          PhotoRepository photoRepo) {
    this.postRepo = postRepo;
    this.postPhotoRepo = postPhotoRepo;
    this.productRepo = productRepo;
    this.photoRepo = photoRepo;
  }

  /*
   * Ensure a canonical Post exists for this product.
   * If one exists, return it.
   * Else create and link the photos.
   */
  public Post ensurePostForProduct(int productId) {

    // 1) Load product + photos
    Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(
            "Product not found productId=" + productId));

    List<Photo> photos = photoRepo.findByProductId(productId);

    // 2) Derive title/caption/etc (same as before)
    String title = buildTitle(product);
    String caption = buildCaption(product, photos);
    String languageCode = "pt-BR";   // or derive
    ZonedDateTime desiredPublishTime = computeDesiredPublishTime(product);

    // 3) Check if there is already an ACTIVE post for this product
    Optional<Post> existingOpt = postRepo.findActiveByProductId(productId);

    Post post;

    if (existingOpt.isPresent()) {
      // UPDATE path: keep the same postId, refresh metadata + photos
      Post existing = existingOpt.get();
      post = existing.withUpdatedMetadata(
              title,
              caption,
              languageCode,
              desiredPublishTime,
              true, // keep active
              existing.getNotes() // or null/whatever you want
      );

      postRepo.update(post);

      // Clear old post_photo rows and re-insert
      postPhotoRepo.deleteByPostId(post.getPostId());
      createPostPhotos(post, photos);

    } else {
      // INSERT path: create new canonical post for this product
      Post newPost = new Post(
              productId,
              title,
              caption,
              languageCode,
              ZonedDateTime.now().toOffsetDateTime(), // createdAt
              desiredPublishTime.toOffsetDateTime(),
              true, // is_active
              null // notes
      );

      post = postRepo.insert(newPost);
      createPostPhotos(post, photos);
    }

    return post;
  }

  private void createPostPhotos(Post post, List<Photo> photos) {
    if (photos == null || photos.isEmpty()) {
      return;
    }

    boolean first = true;
    int sortOrder = 0;

    for (Photo photo : photos) {
      boolean isPrimary = first;
      first = false;

      PostPhoto pp = new PostPhoto(
              post.getPostId(),
              photo.getPhotoId(),
              sortOrder++,
              isPrimary
      );
      postPhotoRepo.insert(pp);
    }
  }

  private ZonedDateTime computeDesiredPublishTime(Product product) {
    return ZonedDateTime.now();
  }

  private String buildCaption(Product product, List<Photo> photos) {
    return null;
  }
  
  /**
   * 
   * @param product
   * @return 
   */
  private String buildTitle(Product product) {
    return product.name;
  }

}
