
import common.db.DbConnection;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.Test;
import used_furniture.core.products.model.Product;
import used_furniture.core.ProductsSource;
import used_furniture.core.ProductsStore;

/**
 *
 * @author rmarq
 */
public class ProductsSourceIT extends BaseIT {

  @Test
  public void test_read_products() {
    DbConnection conn = this.getDbConnection();
    ProductsSource source = new ProductsSource(conn);
    List<Product> products = source.getProducts(false);
    products.stream().forEach(System.out::println);
  }
  
  
  @Test
  public void test_add_then_updateproduct() {
    DbConnection conn = this.getDbConnection();
    ProductsStore instance = new ProductsStore(conn);
    String name = "";
    String description = "";
    ZonedDateTime dateReceived = ZonedDateTime.now();
    ZonedDateTime dateSold = null;
    int category = 0;
    Product product = new Product( //
            -1, name, description, true, //
            dateReceived, dateSold, category, null, 0, 0, 0, 0, false, false);
    int newProductId = instance.addProduct(product);
    product = product.setProductId(newProductId);
    instance.updateProduct(product);
    instance.removeProduct(newProductId);
    System.out.println("d = " + newProductId);
  }


}
