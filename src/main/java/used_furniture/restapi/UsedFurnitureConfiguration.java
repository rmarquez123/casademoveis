package used_furniture.restapi;

import com.zaxxer.hikari.HikariConfig;
import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.posts.repository.PostPhotoRepository;
import used_furniture.core.posts.repository.PostPublicationRepository;
import used_furniture.core.posts.repository.PostRepository;
import used_furniture.core.products.repository.PhotoRepository;
import used_furniture.core.products.repository.ProductRepository;
import used_furniture.restapi.posts.client.FacebookPublisher;
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
 *
 * @author rmarq
 */
@Configuration
public class UsedFurnitureConfiguration {

  @Bean("used_furniture.conn")
  public DbConnection dbconn(@Qualifier("appProps") Properties appProps) {

    String url = appProps.getProperty("used-furniture.db.url");
    int port = Integer.parseInt(appProps.getProperty("used-furniture.db.port"));
    String databaseName = appProps.getProperty("used-furniture.db.name");
    String user = appProps.getProperty("used-furniture.db.user");
    String password = appProps.getProperty("used-furniture.db.password");
    Consumer<HikariConfig> configHelper = p -> {
    };
    ConnectionPool connPool = new HikariConnectionPool( //
            url, port, databaseName, user, password, configHelper);
    return new DbConnection(connPool);
  }

  @Bean
  public ProductRepository productRepository(@Qualifier("used_furniture.conn") DbConnection dbconn) {
    return new ProductRepositoryDbImpl(dbconn);
  }

  @Bean
  public PhotoRepository photoRepository(@Qualifier("used_furniture.conn") DbConnection dbconn) {
    return new PhotoRepositoryDbImpl(dbconn);
  }

  @Bean
  public PostRepository postRepository(@Qualifier("used_furniture.conn") DbConnection dbconn) {
    return new PostRepositoryDbImpl(dbconn);
  }

  @Bean
  public PostPhotoRepository PostPhotoRepository(@Qualifier("used_furniture.conn") DbConnection dbconn) {
    return new PostPhotoRepositoryDbImpl(dbconn);
  }

  @Bean
  public PostPublicationRepository postPublicationRepository(@Qualifier("used_furniture.conn") DbConnection dbconn) {
    return new PostPublicationRepositoryDbImpl(dbconn);
  }

  @Bean
  public PostCreationService postCreationService(
          PostRepository postRepo,
          PostPhotoRepository postPhotoRepo,
          ProductRepository productRepo,
          PhotoRepository photoRepo
  ) {
    return new PostCreationService(postRepo, postPhotoRepo, productRepo, photoRepo);
  }

  @Bean
  public CaptionBuilderService captionBuilderService() {
    return new CaptionBuilderService();
  }

  /**
   *
   * @return
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   *
   * @param restTemplate
   * @param appProps
   * @return
   */
  @Bean
  public SocialPublisher facebookPublisher(RestTemplate restTemplate,
          @Qualifier("appProps") Properties appProps) {

    String baseUrl = appProps.getProperty("facebook.graph.api.baseUrl", "https://graph.facebook.com/v21.0");
    String pageId = appProps.getProperty("facebook.page.id");
    String accessToken = appProps.getProperty("facebook.access.token");
    String imageBaseUrl = appProps.getProperty("facebook.image.baseUrl");

    return new FacebookPublisher(restTemplate, baseUrl, pageId, accessToken, imageBaseUrl);
  }

  @Bean
  public SocialPublisher instagramFakePublisher() {
    // Still using fake for Instagram for now
    return new FakeSocialPublisher(SocialPlatform.INSTAGRAM);
  }

  @Bean
  public Map<SocialPlatform, SocialPublisher> publisherByPlatform(
          SocialPublisher facebookPublisher,
          SocialPublisher instagramFakePublisher) {

    Map<SocialPlatform, SocialPublisher> map = new EnumMap<>(SocialPlatform.class);
    map.put(SocialPlatform.FACEBOOK, facebookPublisher);
    map.put(SocialPlatform.INSTAGRAM, instagramFakePublisher);
    return map;
  }

  @Bean
  public PostPublicationService postPublicationService(
          PostPublicationRepository publicationRepo,
          PostRepository postRepo,
          PostPhotoRepository postPhotoRepo,
          ProductRepository productRepo,
          PhotoRepository photoRepo,
          CaptionBuilderService captionBuilderService,
          Map<SocialPlatform, SocialPublisher> publisherByPlatform) {

    return new PostPublicationService(
            publicationRepo,
            postRepo,
            postPhotoRepo,
            productRepo,
            photoRepo,
            captionBuilderService,
            publisherByPlatform
    );
  }

}
