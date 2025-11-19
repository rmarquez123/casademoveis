package used_furniture.restapi;

import com.zaxxer.hikari.HikariConfig;
import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author rmarq
 */
@Configuration
public class UsedFurnitureConfiguration {
  
  @Bean("used_furniture.conn")
  public DbConnection dbconn() {
    String url = "localhost";  
    int port = 5433;
    String databaseName = "casademoveis";
    String user = "postgres"; 
    String password = "postgres"; 
    Consumer<HikariConfig> configHelper = p -> {
    };
    ConnectionPool connPool = new HikariConnectionPool(url, port, databaseName, user, password, configHelper);
    return new DbConnection(connPool);  
  }
}
