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
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.products.repository.PhotoRepository;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.restapi.posts.client.FacebookPostResponse;
import used_furniture.restapi.posts.client.FacebookPublisher;
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
 * Real end-to-end test that:
 *
 * 1) Creates/ensures a Post from an existing product 2) Schedules a FACEBOOK publication
 * 3) Processes due publications using the real FacebookPublisher 4) Asserts the
 * PostPublication row is PUBLISHED with a platformPostId
 *
 * IMPORTANT: - This will create a REAL post on your Facebook Page. - Requires: -
 * facebook.page.id - facebook.access.token - facebook.graph.api.baseUrl -
 * facebook.image.baseUrl in src/test/resources/used-furniture-test.properties (or adjust
 * loader). - Requires a real product with at least one photo: testProductId.
 */
public class PostFromProductAndPublishFacebookApiIT {

  private MockMvc mockMvc;
  private DbConnection dbconn;

  private ProductRepository productRepo;
  private PhotoRepository photoRepo;
  private PostRepository postRepo;
  private used_furniture.core.posts.repository.PostPhotoRepository postPhotoRepo;
  private PostPublicationRepository postPublicationRepo;

  private Properties testProps;

  /* Use an existing product that has at least one photo */
  private int testProductId = 20;

  @Before
  public void setUp() throws Exception {

    this.testProps = loadTestProps();

    /*
     * Skip this test if Facebook config is not present.
     * Similar pattern to your OPENAI integration test.
     */
    String pageId = testProps.getProperty("facebook.page.id");
//    String accessToken = System.getenv("FACEBOOK_API_KEY");
    String accessToken = "EAARHJOAiqzkBQKEsPEtlhHazqUEGZA9OikPAqs6D3KSFlvJQ4O1Jn6h9FjSkzsYazvtjvYME53YbHTrdgln0MP8wQ5v7MTI1SQP4VGrtzDyycKwkzjwSNxhrAZBWV0TxGDQdjsqiDapfgrYuZBbird8LdHZBdNy9dMeIW0Lz7RZCnqC5jwAxjNTblcUkRmZBuSTZCZBVoXZBQ7cEgIuzlnKbI67UmpOjBSxiDn6nzLY4A";
    
      
    /* DB connection */
    this.dbconn = loadTestDbConnection(testProps);

    /* Repositories */
    this.productRepo = new ProductRepositoryDbImpl(dbconn);
    this.photoRepo = new PhotoRepositoryDbImpl(dbconn);
    this.postRepo = new PostRepositoryDbImpl(dbconn);
    this.postPhotoRepo = new PostPhotoRepositoryDbImpl(dbconn);
    this.postPublicationRepo = new PostPublicationRepositoryDbImpl(dbconn);

    /* Services */
    PostCreationService postCreationService
            = new PostCreationService(postRepo, postPhotoRepo, productRepo, photoRepo);

    CaptionBuilderService captionBuilderService = new CaptionBuilderService();

    RestTemplate restTemplate = new RestTemplate();
    String graphApiBaseUrl = testProps.getProperty("facebook.graph.api.baseUrl",
            "https://graph.facebook.com/v21.0");
    String imageBaseUrl = testProps.getProperty("facebook.image.baseUrl");

    SocialPublisher facebookPublisher
            = new FacebookPublisher(restTemplate, graphApiBaseUrl, pageId, accessToken, imageBaseUrl);

    Map<SocialPlatform, SocialPublisher> publisherByPlatform
            = new EnumMap<>(SocialPlatform.class);
    publisherByPlatform.put(SocialPlatform.FACEBOOK, facebookPublisher);

    PostPublicationService postPublicationService
            = new PostPublicationService(
                    postPublicationRepo,
                    postRepo,
                    postPhotoRepo,
                    productRepo,
                    photoRepo,
                    captionBuilderService,
                    publisherByPlatform
            );

    /* Controller wiring */
    PostAdminController controller
            = new PostAdminController(
                    postCreationService,
                    postPublicationService,
                    postRepo,
                    postPublicationRepo
            );

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void fullFlow_realFacebookPublisher_shouldPublish() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    /* 1) Ensure Post from Product */
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
    assertEquals(testProductId, productIdReturned);

    /* 2) Schedule FACEBOOK publication */
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
    String initialStatus = pubNode.get("status").asText();
    assertEquals("PENDING", initialStatus);

    /* 3) Process due publications (real call to Facebook) */
    mockMvc.perform(
            post("/api/social/process-due")
                    .param("limit", "10")
    )
            .andExpect(status().isOk())
            .andExpect(content().string(
                    org.hamcrest.Matchers.containsString("Processed up to")));

    /* 4) Reload PostPublication and assert PUBLISHED with platformPostId */
    PostPublication pub = postPublicationRepo
            .findById(postPublicationId)
            .orElseThrow(() -> new IllegalStateException("PostPublication not found in DB"));

    if (pub.getStatus() == PublicationStatus.FAILED) {
      String msg = "Facebook publish FAILED. Error message from DB: "
              + pub.getErrorMessage();
      System.out.println(msg);
      org.junit.Assert.fail(msg);
    }

    assertEquals(PublicationStatus.PUBLISHED, pub.getStatus());
    assertNotNull("Expected platformPostId to be set after real Facebook publish",
            pub.getPlatformPostId());

    assertNotNull("Expected platformPostId to be set after real Facebook publish",
            pub.getPlatformPostId());

    System.out.println("Final publication status: " + pub.getStatus()
            + ", platformPostId=" + pub.getPlatformPostId());
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------
  private Properties loadTestProps() throws Exception {
    Properties props = new Properties();
    try (java.io.InputStream is
            = getClass().getResourceAsStream("/used-furniture-test.properties")) {
      if (is == null) {
        throw new IllegalStateException(
                "Test properties not found: /used-furniture-test.properties");
      }
      props.load(is);
    }
    return props;
  }

  private DbConnection loadTestDbConnection(Properties props) {
    String url = props.getProperty("used-furniture.db.url");
    int port = Integer.parseInt(props.getProperty("used-furniture.db.port"));
    String databaseName = props.getProperty("used-furniture.db.name");
    String user = props.getProperty("used-furniture.db.user");
    String password = props.getProperty("used-furniture.db.password");

    Consumer<HikariConfig> configHelper = cfg -> {
      cfg.setMaximumPoolSize(2);
    };

    ConnectionPool pool
            = new HikariConnectionPool(url, port, databaseName, user, password, configHelper);

    return new DbConnection(pool);
  }
}
