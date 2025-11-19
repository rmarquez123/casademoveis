package used_furniture.restapi;

import com.zaxxer.hikari.HikariConfig;
import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import java.util.Properties;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    ConnectionPool connPool = new HikariConnectionPool(url, port, databaseName, user, password, configHelper);
    return new DbConnection(connPool);
  }
}
