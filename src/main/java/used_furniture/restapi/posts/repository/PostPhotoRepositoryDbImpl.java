package used_furniture.restapi.posts.repository;

import common.db.DbConnection;
import used_furniture.core.posts.model.PostPhoto;
import used_furniture.core.posts.repository.PostPhotoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * JDBC-based implementation of PostPhotoRepository using DbConnections.
 */
public class PostPhotoRepositoryDbImpl implements PostPhotoRepository {

  private final DbConnection dbconn;

  public PostPhotoRepositoryDbImpl(DbConnection conn) {
    this.dbconn = conn;
  }
  
  

  @Override
  public List<PostPhoto> findByPostId(long postId) {
    String sql = """
        SELECT post_photo_id,
               post_id,
               photo_id,
               sort_order,
               is_primary
          FROM posts.post_photo
         WHERE post_id = ?
         ORDER BY sort_order ASC, post_photo_id ASC
        """;

    List<PostPhoto> result = new ArrayList<>();

    try (Connection conn = this.dbconn.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapRowToPostPhoto(rs));
        }
      }
      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error loading PostPhoto list for postId=" + postId, e);
    }
  }

  @Override
  public Optional<PostPhoto> findPrimaryByPostId(long postId) {
    String sql = """
        SELECT post_photo_id,
               post_id,
               photo_id,
               sort_order,
               is_primary
          FROM posts.post_photo
         WHERE post_id = ?
           AND is_primary = TRUE
        """;

    try (Connection conn = this.dbconn.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToPostPhoto(rs));
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error loading primary PostPhoto for postId=" + postId, e);
    }
  }

  @Override
  public PostPhoto insert(PostPhoto postPhoto) {
    String sql = """
        INSERT INTO posts.post_photo (
            post_id,
            photo_id,
            sort_order,
            is_primary
        )
        VALUES (?, ?, ?, ?)
        RETURNING post_photo_id
        """;

    try (Connection conn = this.dbconn.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postPhoto.getPostId());
      ps.setLong(2, postPhoto.getPhotoId());
      ps.setInt(3, postPhoto.getSortOrder());
      ps.setBoolean(4, postPhoto.isPrimary());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          long generatedId = rs.getLong("post_photo_id");
          postPhoto.setPostPhotoId(generatedId);
          return postPhoto;
        } else {
          throw new RuntimeException("Insert into posts.post_photo did not return a generated id");
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error inserting PostPhoto for postId=" + postPhoto.getPostId(), e);
    }
  }

  @Override
  public void deleteByPostId(long postId) {
    String sql = """
        DELETE FROM posts.post_photo
         WHERE post_id = ?
        """;

    try (Connection conn = this.dbconn.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);
      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error deleting PostPhoto rows for postId=" + postId, e);
    }
  }

  private PostPhoto mapRowToPostPhoto(ResultSet rs) throws SQLException {
    PostPhoto pp = new PostPhoto();
    pp.setPostPhotoId(rs.getLong("post_photo_id"));
    pp.setPostId(rs.getLong("post_id"));
    pp.setPhotoId(rs.getLong("photo_id"));
    pp.setSortOrder(rs.getInt("sort_order"));
    pp.setPrimary(rs.getBoolean("is_primary"));
    return pp;
  }
}
