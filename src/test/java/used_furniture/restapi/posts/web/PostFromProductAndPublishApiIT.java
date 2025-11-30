package used_furniture.restapi.posts.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.products.repository.PhotoRepository;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.restapi.posts.client.FakeSocialPublisher;
import used_furniture.restapi.posts.client.SocialPublisher;
import used_furniture.restapi.posts.repository.PostPhotoRepositoryDbImpl;
import used_furniture.restapi.posts.repository.PostPublicationRepositoryDbImpl;
import used_furniture.restapi.posts.repository.PostRepositoryDbImpl;
import used_furniture.restapi.posts.service.CaptionBuilderService;
import used_furniture.restapi.posts.service.PostCreationService;
import used_furniture.restapi.posts.service.PostPublicationService;
import used_furniture.restapi.products.repository.PhotoRepositoryDbImpl;
import used_furniture.restapi.products.repository.ProductRepositoryDbImpl;

/**
 * End-to-end style test for the full social flow:
 *
 *  1) POST /api/social/post-from-product
 *  2) POST /api/social/schedule (FACEBOOK)
 *  3) POST /api/social/process-due
 *  4) Verify posts.post_publication row is PUBLISHED
 *
 * IMPORTANT:
 *  - Uses a real productId (testProductId).
 *  - Requires a running PostgreSQL with products/* and posts/* tables.
 *  - Uses FakeSocialPublisher for FACEBOOK to avoid external API calls.
 */
public class PostFromProductAndPublishApiIT {

  private MockMvc mockMvc;
  private DbConnection dbconn;

  private ProductRepository productRepo;
  private PhotoRepository photoRepo;
  private PostRepository postRepo;
  private used_furniture.core.posts.repository.PostPhotoRepository postPhotoRepo;
  private PostPublicationRepository postPublicationRepo;

  // Use an existing product that has at least one photo
  private final int testProductId = 20;

  @Before
  public void setUp() throws Exception {
    // --- Build DbConnection for test DB ---
    this.dbconn = loadTestDbConnection();

    // --- Concrete repositories using DbConnection ---
    this.productRepo = new ProductRepositoryDbImpl(dbconn);
    this.photoRepo = new PhotoRepositoryDbImpl(dbconn);
    this.postRepo = new PostRepositoryDbImpl(dbconn);
    this.postPhotoRepo = new PostPhotoRepositoryDbImpl(dbconn);
    this.postPublicationRepo = new PostPublicationRepositoryDbImpl(dbconn);

    // --- Services ---
    PostCreationService postCreationService =
        new PostCreationService(postRepo, postPhotoRepo, productRepo, photoRepo);

    CaptionBuilderService captionBuilderService = new CaptionBuilderService();

    // Fake publisher for FACEBOOK (no real HTTP calls)
    SocialPublisher facebookFakePublisher = new FakeSocialPublisher(SocialPlatform.FACEBOOK);
    Map<SocialPlatform, SocialPublisher> publisherByPlatform =
        new EnumMap<>(SocialPlatform.class);
    publisherByPlatform.put(SocialPlatform.FACEBOOK, facebookFakePublisher);

    PostPublicationService postPublicationService =
        new PostPublicationService(
            postPublicationRepo,
            postRepo,
            postPhotoRepo,
            productRepo,
            photoRepo,
            captionBuilderService,
            publisherByPlatform
        );

    // --- Controller wiring ---
    PostAdminController controller =
        new PostAdminController(
            postCreationService,
            postPublicationService,
            postRepo,
            postPublicationRepo
        );

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void fullFlow_shouldPublishFacebookPost() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    // 1) Create/ensure Post from Product
    String createBody = "{ \"productId\": " + testProductId + " }";

    MvcResult createResult = mockMvc.perform(
            post("/api/social/post-from-product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String createResponseJson = createResult.getResponse().getContentAsString();
    System.out.println("Response from post-from-product: " + createResponseJson);

    JsonNode postNode = mapper.readTree(createResponseJson);
    long postId = postNode.get("postId").asLong();
    int productIdReturned = postNode.get("productId").asInt();
    // Basic sanity check
    assertEquals(testProductId, productIdReturned);

    // 2) Schedule a FACEBOOK publication for this post
    String scheduleJson = "{"
        + "\"postId\": " + postId + ","
        + "\"platform\": \"FACEBOOK\","
        + "\"targetAccount\": \"Casa de Móveis Usados\","
        + "\"captionOverride\": null,"
        + "\"scheduledTime\": null"
        + "}";

    MvcResult scheduleResult = mockMvc.perform(
            post("/api/social/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduleJson)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String scheduleResponseJson = scheduleResult.getResponse().getContentAsString();
    System.out.println("Response from /api/social/schedule: " + scheduleResponseJson);

    JsonNode pubNode = mapper.readTree(scheduleResponseJson);
    long postPublicationId = pubNode.get("postPublicationId").asLong();
    String status = pubNode.get("status").asText();
    // After scheduling, status should be PENDING
    assertEquals("PENDING", status);

    // 3) Process due publications (FakeSocialPublisher will "publish")
    mockMvc.perform(
            post("/api/social/process-due")
                .param("limit", "10")
        )
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Processed up to")));

    // 4) Reload PostPublication from DB and assert it is PUBLISHED
    PostPublication pub = postPublicationRepo
        .findById(postPublicationId)
        .orElseThrow(() -> new IllegalStateException("PostPublication not found in DB"));

    assertEquals(PublicationStatus.PUBLISHED, pub.getStatus());
    assertNotNull("Expected platformPostId to be set after publishing", pub.getPlatformPostId());

    System.out.println("Final publication status: " + pub.getStatus()
        + ", platformPostId=" + pub.getPlatformPostId());
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------
  private DbConnection loadTestDbConnection() throws Exception {
    Properties props = new Properties();
    try (java.io.InputStream is =
             getClass().getResourceAsStream("/used-furniture-test.properties")) {
      if (is == null) {
        throw new IllegalStateException(
            "Test DB properties not found: /used-furniture-test.properties");
      }
      props.load(is);
    }

    String url = props.getProperty("used-furniture.db.url");
    int port = Integer.parseInt(props.getProperty("used-furniture.db.port"));
    String databaseName = props.getProperty("used-furniture.db.name");
    String user = props.getProperty("used-furniture.db.user");
    String password = props.getProperty("used-furniture.db.password");

    Consumer<HikariConfig> configHelper = cfg -> {
      cfg.setMaximumPoolSize(2);
    };

    ConnectionPool pool =
        new HikariConnectionPool(url, port, databaseName, user, password, configHelper);

    return new DbConnection(pool);
  }
}
