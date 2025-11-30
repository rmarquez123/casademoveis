package used_furniture.restapi.posts.client;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


/*
 * Real Facebook publisher using the Graph API.
 *
 * Strategy:
 *  - If we have at least one photo, post to /{page-id}/photos with:
 *      message = caption
 *      url     = publicly accessible image URL (derived from imageBaseUrl + photoId)
 *  - If there is no photo, fall back to /{page-id}/feed with:
 *      message = caption
 */
public class FacebookPublisher implements SocialPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(FacebookPublisher.class);

  private final RestTemplate restTemplate;
  private final String graphApiBaseUrl;   // e.g. https://graph.facebook.com/v21.0
  private final String pageId;            // your Facebook Page ID
  private final String accessToken;       // long-lived page access token
  private final String imageBaseUrl;      // e.g. https://web.casademoveisusados.com/photos/

  public FacebookPublisher(RestTemplate restTemplate,
          String graphApiBaseUrl,
          String pageId,
          String accessToken,
          String imageBaseUrl) {
    this.restTemplate = restTemplate;
    this.graphApiBaseUrl = graphApiBaseUrl;
    this.pageId = pageId;
    this.accessToken = accessToken;
    this.imageBaseUrl = imageBaseUrl;
  }

  @Override
  public SocialPlatform getPlatform() {
    return SocialPlatform.FACEBOOK;
  }

  @Override
  public PublicationResult publish(Post post,
          Product product,
          List<PostPhoto> postPhotos,
          List<Photo> productPhotos,
          String caption) {

    try {
      Optional<String> imageUrlOpt = resolveImageUrl(postPhotos, productPhotos);

      if (imageUrlOpt.isPresent()) {
        return publishPhotoPost(caption, imageUrlOpt.get());
      } else {
        LOG.info("No photo found for postId={}, publishing text-only feed post", post.getPostId());
        return publishFeedPost(caption);
      }

    } catch (RestClientException ex) {
      LOG.error("Error publishing to Facebook pageId={} for postId={}",
              pageId, post.getPostId(), ex);
      return PublicationResult.failure("Facebook publish error: " + ex.getMessage());
    } catch (Exception ex) {
      LOG.error("Unexpected error publishing to Facebook pageId={} for postId={}",
              pageId, post.getPostId(), ex);
      return PublicationResult.failure("Unexpected Facebook error: " + ex.getMessage());
    }
  }

  /*
   * Try to find an image URL from the linked PostPhoto + Product Photo.
   * We assume imageBaseUrl + photoId forms a publicly accessible URL.
   *
   * e.g. imageBaseUrl = "https://restapi.casademoveisusados.com/product/photo/"
   *      photoId = 42
   *      => https://restapi.casademoveisusados.com/product/photo/42
   */
  private Optional<String> resolveImageUrl(List<PostPhoto> postPhotos, List<Photo> productPhotos) {

    if (postPhotos == null || postPhotos.isEmpty()) {
      return Optional.empty();
    }

    // Prefer primary, else first
    PostPhoto chosen = postPhotos.stream()
            .filter(PostPhoto::isPrimary)
            .findFirst()
            .orElse(postPhotos.get(0));

    long chosenPhotoId = chosen.getPhotoId();

    boolean existsInProductPhotos = productPhotos.stream()
            .anyMatch(p -> p.getPhotoId() == chosenPhotoId);

    if (!existsInProductPhotos) {
      // We still can form the URL from photoId, even if not in the loaded productPhotos list,
      // as long as the endpoint is based on photoId.
      LOG.warn("PostPhoto references photoId={} that is not in loaded productPhotos", chosenPhotoId);
    }

    String url = imageBaseUrl.endsWith("/")
            ? imageBaseUrl + chosenPhotoId
            : imageBaseUrl + "/" + chosenPhotoId;

    return Optional.of(url);
  }

  private PublicationResult publishPhotoPost(String caption, String imageUrl) {
    String endpoint = graphApiBaseUrl + "/" + pageId + "/photos";

    LOG.info("Publishing Facebook photo post to pageId={} with imageUrl={}", pageId, imageUrl);

    // Send data as POST form body instead of query params
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("access_token", accessToken);
    form.add("message", caption);
    form.add("url", imageUrl);

    FacebookPostResponse response
            = restTemplate.postForObject(endpoint, form, FacebookPostResponse.class);

    if (response != null && response.getId() != null) {
      LOG.info("Facebook photo post created with id={}", response.getId());
      return PublicationResult.success(response.getId());
    } else {
      String msg = "Facebook photo post returned null or no id";
      LOG.warn(msg);
      return PublicationResult.failure(msg);
    }
  }

  /**
   *
   * @param caption
   * @return
   */
  private PublicationResult publishFeedPost(String caption) {
    String endpoint = graphApiBaseUrl + "/" + pageId + "/feed";

    LOG.info("Publishing Facebook feed (text) post to pageId={}", pageId);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("access_token", accessToken);
    form.add("message", caption);

    FacebookPostResponse response
            = restTemplate.postForObject(endpoint, form, FacebookPostResponse.class);

    if (response != null && response.getId() != null) {
      LOG.info("Facebook feed post created with id={}", response.getId());
      return PublicationResult.success(response.getId());
    } else {
      String msg = "Facebook feed post returned null or no id";
      LOG.warn(msg);
      return PublicationResult.failure(msg);
    }
  }

}
