package used_furniture.restapi.posts.repository;

import common.db.DbConnection;
import used_furniture.core.posts.model.PostPublication;
import used_furniture.core.posts.model.PublicationStatus;
import used_furniture.core.posts.model.SocialPlatform;
import used_furniture.core.posts.repository.PostPublicationRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * JDBC-based implementation of PostPublicationRepository using DbConnections.
 */
public class PostPublicationRepositoryDbImpl implements PostPublicationRepository {

  private final DbConnection dbconn;

  public PostPublicationRepositoryDbImpl(DbConnection dbconn) {
    this.dbconn = dbconn;
  }

  @Override
  public Optional<PostPublication> findById(long postPublicationId) {
    String sql = baseSelectSql() + " WHERE post_publication_id = ?";

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postPublicationId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToPostPublication(rs));
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error loading PostPublication by id=" + postPublicationId, e);
    }
  }

  @Override
  public Optional<PostPublication> findByPostAndPlatform(long postId, SocialPlatform platform) {
    String sql = baseSelectSql() + " WHERE post_id = ? AND platform = ?";

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);
      ps.setString(2, platform.name());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapRowToPostPublication(rs));
        }
      }
      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException(
              "Error loading PostPublication for postId=" + postId + " and platform=" + platform, e);
    }
  }

  @Override
  public PostPublication insert(PostPublication publication) {
    String sql = """
        INSERT INTO posts.post_publication (
            post_id,
            platform,
            target_account,
            caption_override,
            status,
            scheduled_time,
            published_at,
            platform_post_id,
            error_message,
            last_attempt_at,
            attempt_count
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING post_publication_id
        """;

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, publication.getPostId());
      ps.setString(2, publication.getPlatform().name());
      ps.setString(3, publication.getTargetAccount());
      ps.setString(4, publication.getCaptionOverride());
      ps.setString(5, publication.getStatus().name());
      ps.setObject(6, publication.getScheduledTime());
      ps.setObject(7, publication.getPublishedAt());
      ps.setString(8, publication.getPlatformPostId());
      ps.setString(9, publication.getErrorMessage());
      ps.setObject(10, publication.getLastAttemptAt());
      ps.setInt(11, publication.getAttemptCount());

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          long generatedId = rs.getLong("post_publication_id");
          publication.setPostPublicationId(generatedId);
          return publication;
        } else {
          throw new RuntimeException("Insert into posts.post_publication did not return a generated id");
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error inserting PostPublication for postId=" + publication.getPostId(), e);
    }
  }

  @Override
  public void update(PostPublication publication) {
    if (publication.getPostPublicationId() <= 0) {
      throw new IllegalArgumentException("PostPublication must have a valid id for update");
    }

    String sql = """
        UPDATE posts.post_publication
           SET post_id          = ?,
               platform         = ?,
               target_account   = ?,
               caption_override = ?,
               status           = ?,
               scheduled_time   = ?,
               published_at     = ?,
               platform_post_id = ?,
               error_message    = ?,
               last_attempt_at  = ?,
               attempt_count    = ?
         WHERE post_publication_id = ?
        """;

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, publication.getPostId());
      ps.setString(2, publication.getPlatform().name());
      ps.setString(3, publication.getTargetAccount());
      ps.setString(4, publication.getCaptionOverride());
      ps.setString(5, publication.getStatus().name());
      ps.setObject(6, publication.getScheduledTime());
      ps.setObject(7, publication.getPublishedAt());
      ps.setString(8, publication.getPlatformPostId());
      ps.setString(9, publication.getErrorMessage());
      ps.setObject(10, publication.getLastAttemptAt());
      ps.setInt(11, publication.getAttemptCount());
      ps.setLong(12, publication.getPostPublicationId());

      ps.executeUpdate();

    } catch (SQLException e) {
      throw new RuntimeException("Error updating PostPublication id=" + publication.getPostPublicationId(), e);
    }
  }

  @Override
  public List<PostPublication> findDuePublications(OffsetDateTime now, int limit) {
    String sql = baseSelectSql() + """
          WHERE status IN ('PENDING', 'QUEUED')
            AND (scheduled_time IS NULL OR scheduled_time <= ?)
         ORDER BY scheduled_time NULLS FIRST, post_publication_id
         LIMIT ?
        """;

    List<PostPublication> result = new ArrayList<>();

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setObject(1, now);
      ps.setInt(2, limit);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapRowToPostPublication(rs));
        }
      }
      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error finding due PostPublications", e);
    }
  }

  @Override
  public List<PostPublication> findByStatus(PublicationStatus status, int limit) {
    String sql = baseSelectSql() + """
          WHERE status = ?
         ORDER BY post_publication_id
         LIMIT ?
        """;

    List<PostPublication> result = new ArrayList<>();

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, status.name());
      ps.setInt(2, limit);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapRowToPostPublication(rs));
        }
      }
      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error finding PostPublications by status=" + status, e);
    }
  }

  private String baseSelectSql() {
    return """
        SELECT post_publication_id,
               post_id,
               platform,
               target_account,
               caption_override,
               status,
               scheduled_time,
               published_at,
               platform_post_id,
               error_message,
               last_attempt_at,
               attempt_count
          FROM posts.post_publication
        """;
  }

  private PostPublication mapRowToPostPublication(ResultSet rs) throws SQLException {
    PostPublication p = new PostPublication();
    p.setPostPublicationId(rs.getLong("post_publication_id"));
    p.setPostId(rs.getLong("post_id"));

    String platformStr = rs.getString("platform");
    p.setPlatform(platformStr != null ? SocialPlatform.valueOf(platformStr) : null);

    p.setTargetAccount(rs.getString("target_account"));
    p.setCaptionOverride(rs.getString("caption_override"));

    String statusStr = rs.getString("status");
    p.setStatus(statusStr != null ? PublicationStatus.valueOf(statusStr) : null);

    p.setScheduledTime(rs.getObject("scheduled_time", OffsetDateTime.class));
    p.setPublishedAt(rs.getObject("published_at", OffsetDateTime.class));
    p.setPlatformPostId(rs.getString("platform_post_id"));
    p.setErrorMessage(rs.getString("error_message"));
    p.setLastAttemptAt(rs.getObject("last_attempt_at", OffsetDateTime.class));
    p.setAttemptCount(rs.getInt("attempt_count"));

    return p;
  }

  @Override
  public List<PostPublication> findByPost(long postId) {
    String sql = """
      SELECT post_publication_id,
             post_id,
             platform,
             target_account,
             caption_override,
             status,
             scheduled_time,
             published_at,
             platform_post_id,
             error_message,
             last_attempt_at,
             attempt_count
        FROM posts.post_publication
       WHERE post_id = ?
      """;
    List<PostPublication> result = new ArrayList<>();

    try (Connection conn = this.dbconn.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, postId);
      
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          result.add(mapRowToPostPublication(rs));
        }
      }
      return result;

    } catch (SQLException e) {
      throw new RuntimeException("Error finding PostPublications by postId=" + postId, e);
    }
  }

}
