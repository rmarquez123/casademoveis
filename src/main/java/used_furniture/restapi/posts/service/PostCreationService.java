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
import java.util.List;

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

    List<Post> existingPosts = postRepo.findByProductId(productId);
    if (!existingPosts.isEmpty()) {
      return existingPosts.get(0); // take the most recently created
    }

    Product product = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

    Post newPost = new Post(
        product.getProductId(),
        product.getName(),   // default title from product
        null,                // caption built later by builder
        "pt-BR",             // default language
        OffsetDateTime.now(),// default desired publish time
        true,                // active
        null                 // no notes
    );

    // Insert canonical post
    newPost = postRepo.insert(newPost);

    // Attach product photos
    List<Photo> productPhotos = photoRepo.findByProductId(productId);
    int sortOrder = 0;
    for (Photo photo : productPhotos) {
      PostPhoto pp = new PostPhoto(
          newPost.getPostId(),
          photo.getPhotoId(),
          sortOrder++,
          sortOrder == 0 // primary if first
      );
      postPhotoRepo.insert(pp);
    }
    return newPost;
  }
}
