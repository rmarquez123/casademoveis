package used_furniture.core.products.repository;

import java.util.List;
import used_furniture.core.products.model.Photo;

/*
 * Data access contract for products.photo.
 */
public interface PhotoRepository {

  /*
   * Load all photos for a given product.
   */
  List<Photo> findByProductId(int productId);
}
