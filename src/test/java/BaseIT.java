
import com.zaxxer.hikari.HikariConfig;
import common.db.ConnectionPool;
import common.db.DbConnection;
import common.db.HikariConnectionPool;
import java.util.function.Consumer;

/**
 *
 * @author rmarq
 */
public class BaseIT {
  /**
   *
   * @return
   */
  protected DbConnection getDbConnection() {
    String url = "casademoveisusados.com";
    int port = 5432;
    String databaseName = "casademoveis";
    String user = "postgres";
    String password = "postgres";
    Consumer<HikariConfig> configHelper = p -> {
    };
    ConnectionPool connPool = new HikariConnectionPool(url, port, databaseName, user, password, configHelper);
    return new DbConnection(connPool);
  }  
}
