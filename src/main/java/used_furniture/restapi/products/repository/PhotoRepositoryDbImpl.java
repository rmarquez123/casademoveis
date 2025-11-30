package used_furniture.restapi.products.repository;

import common.db.DbConnection;
import used_furniture.core.products.model.Photo;
import used_furniture.core.products.repository.PhotoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * JDBC-based implementation of PhotoRepository using DbConnection.
 */
public class PhotoRepositoryDbImpl implements PhotoRepository {

  private final DbConnection dbconn;

  public PhotoRepositoryDbImpl(DbConnection dbconn) {
    this.dbconn = dbconn;
  }

  @Override
  public List<Photo> findByProductId(int productId) {

    String sql = """
        SELECT photo_id,
               product_id,
               photo
          FROM products.photo
         WHERE product_id = ?
         ORDER BY photo_id
        """;

    List<Photo> result = new ArrayList<>();

    try (Connection conn = dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, productId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          long photoId = rs.getLong("photo_id");
          long prodId = rs.getLong("product_id");
          byte[] bytes = rs.getBytes("photo");

          result.add(new Photo(photoId, prodId, bytes));
        }
      }

      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error loading Photos for productId=" + productId, e);
    }
  }
}
