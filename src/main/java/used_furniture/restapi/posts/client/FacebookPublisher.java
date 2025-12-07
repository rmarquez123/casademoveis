package used_furniture.restapi.posts.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
      // Collect up to 10 image URLs based on PostPhoto + productPhotos
      List<String> imageUrls = resolveImageUrls(postPhotos, productPhotos, 10);

      if (imageUrls.isEmpty()) {
        LOG.info("No photos found for postId={}, publishing text-only feed post", post.getPostId());
        return publishFeedPost(caption);
      }

      if (imageUrls.size() == 1) {
        String imageUrl = imageUrls.get(0);
        return publishPhotoPost(caption, imageUrl);
      } else {
        LOG.info("Publishing multi-photo Facebook post: postId={} photoCount={}",
                post.getPostId(), imageUrls.size());
        return publishMultiPhotoPost(caption, imageUrls);
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
 * Build a list of image URLs for this post, based on PostPhoto rows.
 * - Primary photos first, then by sort_order, then by photoId.
 * - Uses imageBaseUrl + "/" + photoId.
   */
  private List<String> resolveImageUrls(List<PostPhoto> postPhotos,
          List<Photo> productPhotos,
          int maxImages) {

    List<String> urls = new ArrayList<>();
    if (postPhotos == null || postPhotos.isEmpty()) {
      return urls;
    }

    // Sort: primary first, then by sort_order, then photoId
    List<PostPhoto> sorted = new ArrayList<>(postPhotos);
    sorted.sort(Comparator
            .comparing(PostPhoto::isPrimary).reversed()
            .thenComparingInt(PostPhoto::getSortOrder)
            .thenComparingLong(PostPhoto::getPhotoId));

    Set<Long> seenPhotoIds = new HashSet<>();
    for (PostPhoto pp : sorted) {
      if (urls.size() >= maxImages) {
        break;
      }
      long photoId = pp.getPhotoId();
      if (!seenPhotoIds.add(photoId)) {
        continue;  // skip duplicate
      }

      String url = imageBaseUrl.endsWith("/")
              ? imageBaseUrl + photoId
              : imageBaseUrl + "/" + photoId;

      urls.add(url);
    }

    return urls;
  }

  /*
   * Upload a photo as UNPUBLISHED to Facebook and return its media_fbid.
   */
  private String uploadUnpublishedPhoto(String imageUrl) {
    String endpoint = graphApiBaseUrl + "/" + pageId + "/photos";

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("access_token", accessToken);
    body.add("url", imageUrl);
    body.add("published", "false");

    LOG.info("Uploading unpublished photo to Facebook: pageId={} imageUrl={}", pageId, imageUrl);

    FacebookPostResponse response
            = restTemplate.postForObject(endpoint, body, FacebookPostResponse.class);

    if (response == null || response.getId() == null) {
      String msg = "Unpublished photo upload returned null or no id for url=" + imageUrl;
      LOG.warn(msg);
      throw new IllegalStateException(msg);
    }

    LOG.info("Unpublished photo uploaded with media_fbid={}", response.getId());
    return response.getId();
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

  /*
 * Publish a multi-photo feed post using attached_media.
 *
 * Flow:
 *  1) For each image URL, upload as unpublished photo -> get media_fbid.
 *  2) POST /{page-id}/feed with:
 *       message = caption
 *       attached_media[0] = {"media_fbid":"<ID0>"}
 *       attached_media[1] = {"media_fbid":"<ID1>"}
 *       ...
   */
  private PublicationResult publishMultiPhotoPost(String caption, List<String> imageUrls) {

    if (imageUrls == null || imageUrls.isEmpty()) {
      return publishFeedPost(caption);
    }

    // 1) Upload each image as unpublished and collect media_fbid ids
    List<String> mediaFbids = new ArrayList<>();
    for (String url : imageUrls) {
      String mediaFbid = uploadUnpublishedPhoto(url);
      mediaFbids.add(mediaFbid);
    }

    // 2) Build /feed request with attached_media
    String endpoint = graphApiBaseUrl + "/" + pageId + "/feed";

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("access_token", accessToken);
    body.add("message", caption);

    for (int i = 0; i < mediaFbids.size(); i++) {
      String fbid = mediaFbids.get(i);
      // Each entry is a small JSON object telling FB which media_fbid to attach.
      String attachedMediaJson = "{\"media_fbid\":\"" + fbid + "\"}";
      body.add("attached_media[" + i + "]", attachedMediaJson);
    }

    LOG.info("Publishing multi-photo feed post to pageId={} with {} images",
            pageId, mediaFbids.size());

    FacebookPostResponse response
            = restTemplate.postForObject(endpoint, body, FacebookPostResponse.class);

    if (response != null && response.getId() != null) {
      LOG.info("Facebook multi-photo feed post created with id={}", response.getId());
      return PublicationResult.success(response.getId());
    } else {
      String msg = "Facebook multi-photo feed post returned null or no id";
      LOG.warn(msg);
      return PublicationResult.failure(msg);
    }
  }
  
  /**
   * 
   * @param platformPostId
   * @return 
   */
  @Override
  public boolean deletePost(String platformPostId) {
    String endpoint = graphApiBaseUrl + "/" + platformPostId;
    try {
      LOG.info("Deleting Facebook post id={} from pageId={}", platformPostId, pageId);
      // restTemplate.delete throws on non-2xx
      restTemplate.delete(endpoint + "?access_token=" + accessToken);
      return true;
    } catch (RestClientException ex) {
      LOG.error("Failed to delete Facebook post id={} for pageId={}", platformPostId, pageId, ex);
      return false;
    }
  }

}
