package used_furniture.core;

import common.RmObjects;
import common.db.DbConnection;
import java.sql.SQLException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author rmarq
 */
@Component
public class ProductsStore {

  private final DbConnection conn;

  /**
   *
   * @param conn
   */
  public ProductsStore(@Autowired DbConnection conn) {
    this.conn = conn;
  }

  /**
   *
   * @param product
   * @return 
   */
  public int addProduct(Product product) {
    int product_id = this.conn.getNextSequence("product_id", "products.product");
    String statement = "insert into products.product \n"
            + "(product_id, name, description, available, date_recieved, date_sold, category, length, height, depth, price) \n"
            + String.format("values (%d, '%s', '%s', %b, %s, %s, %d, %f, %f, %f, %f)\n",
                    product_id, //
                    product.name, //
                    product.description,// 
                    product.available,//
                    RmObjects.formatUtcForDbStatement(product.dateReceived),// 
                    product.dateSold == null ? "null" : RmObjects.formatUtcForDbStatement(product.dateSold), //
                    product.category, product.length, product.height, product.depth, product.price
                    
            );
    this.conn.executeStatement(statement);
    return product_id;
  }

  /**
   *
   * @param product
   */
  public void updateProduct(Product product) {
    String statement = "update products.product \n"
            + "set (name, description, available, date_recieved, date_sold, category, length, height, depth, price) \n"
            + String.format("= ('%s', '%s', %b, %s, %s, %d, %f, %f, %f, %f)\n",
                    product.name, //
                    product.description,// 
                    product.available,//
                    RmObjects.formatUtcForDbStatement(product.dateReceived),// 
                    product.dateSold == null ? "null" : RmObjects.formatUtcForDbStatement(product.dateSold), //
                    product.category, //
                    product.length, product.height, product.depth, product.price
            )
            + String.format("\nwhere product_id = %d", product.product_id);
    this.conn.executeStatement(statement);
  }

  /**
   *
   * @param productId
   */
  public void removeProduct(int productId) {
    String statement = "delete from products.product  where product_id = " + productId;
    this.conn.executeStatement(statement);
  }

  /**
   *
   * @param photo
   * @return 
   */
  public long addPhoto(Photo photo) {
     
    long photoId = this.conn.getNextSequenceLong("photo_id", "products.photo");
    String statement = "insert into products.photo (photo_id, product_id, photo) values \n"
            + "(?, ?, ?)";
    this.conn.executeStatementsBatch(statement, Arrays.asList(photo), kv -> {
      try {    
        kv.getKey().setLong(1, photoId);  
        kv.getKey().setLong(2, kv.getRight().productId);
        kv.getKey().setBytes(3, kv.getRight().bytes);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    });
    return photoId; 
  }

  /**
   *
   * @param photo
   */
  public void updatePhoto(Photo photo) {
    String statement = "update table products.photo \n"
            + "set (product_id, photo) = (?, ?) \n"
            + String.format("where photo_id = %d \n", photo.photoId);
    this.conn.executeStatementsBatch(statement, Arrays.asList(photo), kv -> {
      try {
        kv.getKey().setLong(1, kv.getRight().productId);
        kv.getKey().setBytes(2, kv.getRight().bytes);
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  /**
   *
   * @param photoId
   */
  public void removePhoto(long photoId) {
    String statement = "delete from products.photo where photo_id = " + photoId;
    this.conn.executeStatement(statement);
  }
}
