package used_furniture.core;

import used_furniture.core.products.model.Photo;
import used_furniture.core.products.model.Product;
import common.db.DbConnection;
import common.db.RmDbUtils;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author rmarq
 */
@Component
public class ProductsSource {

  private final DbConnection conn;

  public ProductsSource(@Autowired @Qualifier("used_furniture.conn") DbConnection conn) {
    this.conn = conn;
  }

  /**
   *
   * @return
   */
  public List<Product> getProducts() {
    String query = "select \n"
            + "p.*, c.name as category_name \n"
            + "from products.product p\n"
            + "join products.category c\n"
            + "on c.category_id = p.category\n";
    ZoneId zoneId = ZoneId.of("UTC");
    List<Product> result = this.conn.executeQuery(query, rs -> {
      int productId = RmDbUtils.intValue(rs, "product_id");
      String name = RmDbUtils.stringValue(rs, "name");
      String description = RmDbUtils.stringValue(rs, "description");
      boolean available = RmDbUtils.booleanValue(rs, "available");
      ZonedDateTime dateReceived = RmDbUtils.getZonedDateTime(rs, "date_recieved", zoneId);
      ZonedDateTime dateSold = RmDbUtils.getZonedDateTime(rs, "date_sold", zoneId);
      int category = RmDbUtils.intValue(rs, "category");
      String categoryName = RmDbUtils.stringValue(rs, "category_name");
      double length = RmDbUtils.doubleValue(rs, "length");
      double height = RmDbUtils.doubleValue(rs, "height");
      double depth = RmDbUtils.doubleValue(rs, "depth");
      double price = RmDbUtils.doubleValue(rs, "price");
      return new Product(productId, name, description, available, dateReceived, dateSold, category, categoryName, length, depth, height, price);
    });
    return result;
  }

  /**
   *
   * @param productIds
   * @return
   */
  public Map<Integer, List<Photo>> getPhotos(List<Integer> productIds) {
    if (productIds.isEmpty()) {
      return new HashMap<>();
    }
    String productIdsText = productIds.stream().map(m -> String.valueOf(m)).collect(Collectors.joining(","));
    String query = "select\n"
            + " * \n"
            + "from products.photo p\n"
            + String.format("where p.product_id = any(array[%s])", productIdsText);
    Map<Integer, List<Photo>> result = new HashMap<>();

    this.conn.executeQuery(query, rs -> {
      int productId = RmDbUtils.intValue(rs, "product_id");
      long photoId = RmDbUtils.intValue(rs, "photo_id");
      byte[] bytes;
      try {
        bytes = rs.getBytes("photo");
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      result.putIfAbsent(productId, new ArrayList<>());
      result.get(productId).add(new Photo(photoId, productId, bytes));
    });
    return result;
  }

  /**
   *
   * @return
   */
  public List<Category> getCategories() {
    String query = "select * from products.category";
    List<Category> result = this.conn.executeQuery(query, rs -> {
      int categoryId = RmDbUtils.intValue(rs, "category_id");
      String name = RmDbUtils.stringValue(rs, "name");
      return new Category(categoryId, name);
    });
    return result;
  }

  /**
   *
   * @param productIds
   * @param width
   * @param height
   * @return
   */
  public Map<Integer, List<Photo>> getPhotos(List<Integer> productIds, Integer width, Integer height) {
    // If no product IDs provided, short-circuit
    if (productIds.isEmpty()) {
      return new HashMap<>();
    }

    // If both width and height are null, just return original images
    if (width == null && height == null) {
      return getPhotos(productIds);
    }

    // 1) Attempt to retrieve existing resized photos for the specified width/height.
    //    This assumes you have columns (width, height) or some logic in your DB
    //    to filter by dimension. Adjust the query as needed.
    String productIdsText = productIds
            .stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    String query = "SELECT * FROM products.photo_sized p "
            + "WHERE p.product_id = ANY(ARRAY[" + productIdsText + "]) "
            + "  AND p.width " + (width == null ? "IS NULL" : "= " + width)
            + "  AND p.height " + (height == null ? "IS NULL" : "= " + height);

    Map<Integer, List<Photo>> result = new HashMap<>();

    // Execute query to find existing matching photos
    this.conn.executeQuery(query, rs -> {
      int productId = RmDbUtils.intValue(rs, "product_id");
      long photoId = RmDbUtils.intValue(rs, "photo_id");
      byte[] bytes;
      try {
        bytes = rs.getBytes("photo");
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      result.putIfAbsent(productId, new ArrayList<>());
      result.get(productId).add(new Photo(photoId, productId, bytes));
    });

    // 2) Identify which product IDs still need resized images (i.e., not found in DB).
    List<Integer> missingPhotoProductIds = productIds
            .stream()
            .filter(pid -> !result.containsKey(pid))
            .collect(Collectors.toList());

    // 3) For each missing product, create the resized images and store them.
    if (!missingPhotoProductIds.isEmpty()) {
      // Get original images for these missing products
      Map<Integer, List<Photo>> originalPhotosMap = getPhotos(missingPhotoProductIds);

      for (Integer productId : missingPhotoProductIds) {
        List<Photo> originalPhotos = originalPhotosMap.getOrDefault(productId, Collections.emptyList());

        // For each original photo, create/store the resized version
        for (Photo origPhoto : originalPhotos) {
          byte[] resizedBytes = resizePhoto(origPhoto.bytes, width, height);

          // Persist the new resized image to the database.
          // This function should be implemented to insert a new record
          // into 'products.photo' (or wherever you're storing) with the
          // correct width/height columns. Return the new photoId if needed.
          long photoId = origPhoto.photoId;
          
          this.persistResizedPhoto(photoId, productId, resizedBytes, width, height);

          // Add the newly created photo into the result Map
          result.putIfAbsent(productId, new ArrayList<>());
          result.get(productId).add(new Photo(photoId, productId, resizedBytes));
        }
      }
    }

    return result;
  }

  /**
   * Example helper (placeholder) for resizing a photo to the specified
   * width/height. In a real implementation, you might use a library like
   * java.awt.Image or something similar.
   */
  private byte[] resizePhoto(byte[] originalBytes, Integer width, Integer height) {
    // Resize logic goes here (e.g., using a Java image-processing library).
    // For now, assume it returns the new image as a byte[].
    return originalBytes;  // placeholder
  }

  /**
   * Example helper (placeholder) for persisting a resized photo to the
   * database. Returns the newly inserted photo_id.
   */
  private long persistResizedPhoto(long photoId, Integer productId, byte[] resizedBytes, Integer width, Integer height) {
    String statement = "insert into products.photo_sized (photo_id, product_id, photo, width, height) values \n"
            + "(?, ?, ?, ?, ?)";
    this.conn.executeStatementsBatch(statement, Arrays.asList(productId), kv -> {
      try {
        kv.getKey().setLong(1, photoId);
        kv.getKey().setLong(2, productId);
        kv.getKey().setBytes(3, resizedBytes);
        kv.getKey().setInt(4, width);
        kv.getKey().setInt(5, height);  
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    });
    return photoId;
  }

}
