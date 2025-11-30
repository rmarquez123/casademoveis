package used_furniture.restapi.posts.repository;

import common.db.DbConnection;
import used_furniture.core.posts.model.Post;
import used_furniture.core.posts.repository.PostRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * JDBC-based implementation of PostRepository using DbConnections.
 */
public class PostRepositoryDbImpl implements PostRepository {

  private final DbConnection dbconn;

  public PostRepositoryDbImpl(DbConnection conn) {
    this.dbconn = conn;
  }

  @Override
  public Optional<Post> findById(long postId) {
    String sql = """
                 SELECT post_id,
                        product_id,
                        title,
                        caption,
                        language_code,
                        created_at,
                        desired_publish_time,
                        is_active,
                        notes
                   FROM posts.post
                  WHERE post_id = ?
                 """;

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToPost(rs));
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error loading Post by id=" + postId, e);
    }
  }

  @Override
  public List<Post> findByProductId(int productId) {
    String sql = """
                 SELECT post_id,
                        product_id,
                        title,
                        caption,
                        language_code,
                        created_at,
                        desired_publish_time,
                        is_active,
                        notes
                   FROM posts.post
                  WHERE product_id = ?
                  ORDER BY created_at DESC, post_id DESC
                 """;

    List<Post> result = new ArrayList<>();

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, productId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapRowToPost(rs));
        }
      }
      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error loading Posts for productId=" + productId, e);
    }
  }

  @Override
  public Post insert(Post post) {
    String sql = """
        INSERT INTO posts.post (
            product_id,
            title,
            caption,
            language_code,
            desired_publish_time,
            is_active,
            notes
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
        RETURNING post_id, created_at
        """;

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, post.getProductId());
      ps.setString(2, post.getTitle());
      ps.setString(3, post.getCaption());
      ps.setString(4, post.getLanguageCode());
      ps.setObject(5, post.getDesiredPublishTime());  // maps to TIMESTAMPTZ
      ps.setBoolean(6, post.isActive());
      ps.setString(7, post.getNotes());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          long generatedId = rs.getLong("post_id");
          OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);

          post.setPostId(generatedId);
          post.setCreatedAt(createdAt);
          return post;
        } else {
          throw new RuntimeException("Insert into posts.post did not return a generated id");
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error inserting Post for productId=" + post.getProductId(), e);
    }
  }

  @Override
  public void update(Post post) {
    if (post.getPostId() <= 0) {
      throw new IllegalArgumentException("Post must have a valid postId for update");
    }

    String sql = """
        UPDATE posts.post
           SET product_id           = ?,
               title                = ?,
               caption              = ?,
               language_code        = ?,
               desired_publish_time = ?,
               is_active            = ?,
               notes                = ?
         WHERE post_id             = ?
        """;

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, post.getProductId());
      ps.setString(2, post.getTitle());
      ps.setString(3, post.getCaption());
      ps.setString(4, post.getLanguageCode());
      ps.setObject(5, post.getDesiredPublishTime());
      ps.setBoolean(6, post.isActive());
      ps.setString(7, post.getNotes());
      ps.setLong(8, post.getPostId());

      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error updating Post id=" + post.getPostId(), e);
    }
  }

  private Post mapRowToPost(ResultSet rs) throws SQLException {
    Post p = new Post();
    p.setPostId(rs.getLong("post_id"));
    p.setProductId(rs.getInt("product_id"));
    p.setTitle(rs.getString("title"));
    p.setCaption(rs.getString("caption"));
    p.setLanguageCode(rs.getString("language_code"));
    p.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    p.setDesiredPublishTime(rs.getObject("desired_publish_time", OffsetDateTime.class));
    p.setActive(rs.getBoolean("is_active"));
    p.setNotes(rs.getString("notes"));
    return p;
  }
}
