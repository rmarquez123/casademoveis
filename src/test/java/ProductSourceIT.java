
import common.db.DbConnection;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import used_furniture.core.ProductsSource;

/**
 *
 * @author rmarq
 */
public class ProductSourceIT extends BaseIT {
  
  
  
  @Test
  public void test() {
    DbConnection conn = this.getDbConnection();
    ProductsSource service = new ProductsSource(conn); 
    List<Integer> productIds = Arrays.asList(28);
    service.getPhotos(productIds,  50, 50); 
  }

}
