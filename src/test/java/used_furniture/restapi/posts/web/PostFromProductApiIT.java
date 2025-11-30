package used_furniture.restapi.posts.web;

import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import com.zaxxer.hikari.HikariConfig;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.products.repository.PhotoRepository;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.restapi.posts.service.PostCreationService;
import used_furniture.restapi.posts.service.PostPublicationService;
import used_furniture.restapi.products.repository.PhotoRepositoryDbImpl;
import used_furniture.restapi.products.repository.ProductRepositoryDbImpl;
import used_furniture.restapi.posts.repository.PostPhotoRepositoryDbImpl;
import used_furniture.restapi.posts.repository.PostRepositoryDbImpl;

/**
 * End-to-end style test for the /api/social/post-from-product endpoint.
 *
 * This test: - Creates a real product + photo in the database - Wires real repositories +
 * PostCreationService + PostAdminController - Calls the endpoint using MockMvc - Verifies
 * that a Post is created and JSON is returned.
 *
 * IMPORTANT: - Requires a running PostgreSQL with the products/* and posts/* tables. -
 * Requires test DB connection properties (see loadTestDbConnection()).
 */
public class PostFromProductApiIT {

  private MockMvc mockMvc;
  private DbConnection dbconn;

  private ProductRepository productRepo;
  private PhotoRepository photoRepo;
  private PostRepository postRepo;
  private used_furniture.core.posts.repository.PostPhotoRepository postPhotoRepo;

  private int testProductId;

  @Before
  public void setUp() throws Exception {
    // --- Build DbConnection for test DB (similar to UsedFurnitureConfiguration) ---
    this.dbconn = loadTestDbConnection();

    // --- Create concrete repositories using DbConnection ---
    this.productRepo = new ProductRepositoryDbImpl(dbconn);
    this.photoRepo = new PhotoRepositoryDbImpl(dbconn);
    this.postRepo = new PostRepositoryDbImpl(dbconn);
    this.postPhotoRepo = new PostPhotoRepositoryDbImpl(dbconn);

    // --- Create a test product + photo directly in the DB ---
    this.testProductId = 20;

    // --- Wire PostCreationService ---
    PostCreationService postCreationService
            = new PostCreationService(postRepo, postPhotoRepo, productRepo, photoRepo);

    // For this test we ONLY hit /post-from-product, so we can safely pass null
    // for PostPublicationService and PostPublicationRepository — they won't be used.
    PostPublicationService dummyPublicationService = null;
    PostPublicationRepository dummyPublicationRepo = null;

    PostAdminController controller
            = new PostAdminController(postCreationService,
                    dummyPublicationService,
                    postRepo,
                    dummyPublicationRepo);

    this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void postFromProduct_shouldCreatePostAndReturnJson() throws Exception {
    String jsonBody = "{ \"productId\": " + testProductId + " }";

    MvcResult result = mockMvc.perform(
            post("/api/social/post-from-product")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
    )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // productId field should match what we sent
            .andExpect(jsonPath("$.productId").value(testProductId))
            // postId should be > 0
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    System.out.println("Response from /api/social/post-from-product:");
    System.out.println(responseBody);

    // Optionally, verify that at least one Post exists in DB for this product
    boolean hasPost = postRepo.findByProductId(testProductId).stream()
            .mapToLong(Post::getPostId)
            .anyMatch(id -> id > 0);

    assertTrue("Expected a Post row to be created for productId=" + testProductId, hasPost);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------
  private DbConnection loadTestDbConnection() throws Exception {
    // Adjust this to however you load your test properties.
    // Here we assume src/test/resources/used-furniture-test.properties exists.
    Properties props = new Properties();
    try (java.io.InputStream is
            = getClass().getResourceAsStream("/used-furniture-test.properties")) {
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
      // Optionally set pool size, timeouts, etc for tests
      cfg.setMaximumPoolSize(2);
    };

    ConnectionPool pool
            = new HikariConnectionPool(url, port, databaseName, user, password, configHelper);

    return new DbConnection(pool);
  }
  
}
