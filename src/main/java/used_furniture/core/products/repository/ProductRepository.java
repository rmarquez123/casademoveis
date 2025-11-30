package used_furniture.core.products.repository;

import java.util.Optional;
import used_furniture.core.products.model.Product;

/*
 * Data access contract for products.product.
 */
public interface ProductRepository {

  /*
   * Load a product by primary key.
   */
  Optional<Product> findById(int productId);
}
