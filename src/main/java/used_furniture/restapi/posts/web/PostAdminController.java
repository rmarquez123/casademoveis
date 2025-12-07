package used_furniture.restapi.posts.web;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.restapi.posts.service.PostCreationService;
import used_furniture.restapi.posts.service.PostPublicationService;

/*
 * Admin / internal controller for managing social posts & publications.
 */
@RestController
@RequestMapping("/api/social")
public class PostAdminController {

  private final PostCreationService postCreationService;
  private final PostPublicationService postPublicationService;
  private final PostRepository postRepository;
  private final PostPublicationRepository postPublicationRepository;

  public PostAdminController(PostCreationService postCreationService,
          PostPublicationService postPublicationService,
          PostRepository postRepository,
          PostPublicationRepository postPublicationRepository) {
    this.postCreationService = postCreationService;
    this.postPublicationService = postPublicationService;
    this.postRepository = postRepository;
    this.postPublicationRepository = postPublicationRepository;
  }

  /*
   * Ensure a canonical Post exists for the given product.
   */
  @PostMapping("/post-from-product")
  public ResponseEntity<Post> createPostFromProduct(@RequestBody CreatePostFromProductRequest request) {

    Post post = postCreationService.ensurePostForProduct(request.getProductId());
    return ResponseEntity.ok(post);
  }

  /*
   * Schedule a publication (e.g. to FACEBOOK) for an existing Post.
   */
  @PostMapping("/schedule")
  public ResponseEntity<PostPublication> schedulePublication(
          @RequestBody SchedulePublicationRequest request) {

    Post post = postRepository.findById(request.getPostId())
            .orElseThrow(() -> new IllegalArgumentException("Post not found id=" + request.getPostId()));

    OffsetDateTime scheduledTime = request.getScheduledTime() != null
            ? request.getScheduledTime()
            : OffsetDateTime.now();

    PostPublication existing = postPublicationRepository
            .findByPostAndPlatform(post.getPostId(), request.getPlatform())
            .orElse(null);

    PostPublication publication;
    if (existing != null) {
      // Update existing schedule
      existing.setScheduledTime(scheduledTime);
      existing.setTargetAccount(request.getTargetAccount());
      existing.setCaptionOverride(request.getCaptionOverride());
      existing.setStatus(PublicationStatus.PENDING);
      existing.setErrorMessage(null);
      existing.setPublishedAt(null);
      postPublicationRepository.update(existing);
      publication = existing;
    } else {
      // Create new
      PostPublication pub = new PostPublication(
              post.getPostId(),
              request.getPlatform(),
              request.getTargetAccount(),
              request.getCaptionOverride(),
              PublicationStatus.PENDING,
              scheduledTime
      );
      publication = postPublicationRepository.insert(pub);
    }

    return ResponseEntity.ok(publication);
  }

  /*
   * Manually trigger processing of due publications (for debugging or cron).
   */
  @PostMapping("/process-due")
  public ResponseEntity<String> processDuePublications(
          @RequestParam(name = "limit", defaultValue = "10") int limit) {

    postPublicationService.processDuePublications(limit);
    return ResponseEntity.ok("Processed up to " + limit + " due publications");
  }

  /*
   * List publications by status (e.g. PENDING) for admin/debug view.
   */
  @GetMapping("/publications")
  public ResponseEntity<List<PublicationStatusResponse>> listPublicationsByStatus(
          @RequestParam(name = "status", defaultValue = "PENDING") PublicationStatus status,
          @RequestParam(name = "limit", defaultValue = "20") int limit) {

    List<PostPublication> pubs = postPublicationRepository.findByStatus(status, limit);

    List<PublicationStatusResponse> response = pubs.stream()
            .map(p -> new PublicationStatusResponse(
            p.getPostPublicationId(),
            p.getPostId(),
            p.getPlatform(),
            p.getStatus(),
            p.getScheduledTime(),
            p.getPublishedAt(),
            p.getPlatformPostId(),
            p.getErrorMessage()
    ))
            .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/post/{postId}")
  public ResponseEntity<Void> deactivatePost(@PathVariable("postId") long postId) {

    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found id=" + postId));

    // 1) Soft delete the post
    postRepository.deactivate(postId);

    // 2) Cancel pending/queued publications
    List<PostPublication> pubs = postPublicationRepository.findByPost(postId);
    for (PostPublication pub : pubs) {
      if (pub.getStatus().isPendingLike()) { // or check PENDING/QUEUED specifically
        pub.setStatus(PublicationStatus.CANCELLED);
        pub.setErrorMessage("Cancelled because post was deactivated");
        postPublicationRepository.update(pub);
      }
    }

    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/post-by-product/{productId}")
  public ResponseEntity<Void> deactivatePostByProduct(@PathVariable("productId") int productId) {
    Optional<Post> existing = postRepository.findActiveByProductId(productId);
    if (existing.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return deactivatePost(existing.get().getPostId());
  }

}
