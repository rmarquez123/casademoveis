package used_furniture.restapi.products.repository;

import common.db.DbConnection;
import used_furniture.core.products.model.Product;
import used_furniture.core.products.repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

/*
 * JDBC-based implementation of ProductRepository using DbConnection.
 */
public class ProductRepositoryDbImpl implements ProductRepository {

  private final DbConnection dbconn;

  public ProductRepositoryDbImpl(DbConnection dbconn) {
    this.dbconn = dbconn;
  }

  @Override
  public Optional<Product> findById(int productId) {

    String sql = """
        SELECT p.product_id,
               p.name,
               p.description,
               p.available,
               p.date_recieved,
               p.date_sold,
               p.category,
               c.name AS category_name,
               p.length,
               p.depth,
               p.height,
               p.price
          FROM products.product p
          LEFT JOIN products.category c
                 ON p.category = c.category_id
         WHERE p.product_id = ?
        """;

    try (Connection conn = dbconn.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, productId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToProduct(rs));
        }
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error loading Product by id=" + productId, e);
    }
  }

  private Product mapRowToProduct(ResultSet rs) throws SQLException {

    int productId = rs.getInt("product_id");
    String name = rs.getString("name");
    String description = rs.getString("description");
    boolean available = rs.getBoolean("available");

    OffsetDateTime dateReceivedOdt = rs.getObject("date_recieved", OffsetDateTime.class);
    OffsetDateTime dateSoldOdt = rs.getObject("date_sold", OffsetDateTime.class);

    ZonedDateTime dateReceived = dateReceivedOdt != null ? dateReceivedOdt.toZonedDateTime() : null;
    ZonedDateTime dateSold = dateSoldOdt != null ? dateSoldOdt.toZonedDateTime() : null;

    int category = rs.getInt("category");
    String categoryName = rs.getString("category_name");

    double length = rs.getDouble("length");
    double depth = rs.getDouble("depth");
    double height = rs.getDouble("height");
    double price = rs.getDouble("price");

    // Product constructor already handles encoding conversion for name/description.
    return new Product(
        productId,
        name,
        description,
        available,
        dateReceived,
        dateSold,
        category,
        categoryName,
        length,
        depth,
        height,
        price
    );
  }
}
