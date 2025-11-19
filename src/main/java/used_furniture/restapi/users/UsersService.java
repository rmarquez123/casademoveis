package used_furniture.restapi.users;

import static common.RmObjects.formatUtc;
import common.db.DbConnection;
import common.db.RmDbUtils;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author rmarq
 */
@Component
public class UsersService {

  private final DbConnection conn;

  /**
   *
   * @param conn
   */
  public UsersService(@Autowired
          @Qualifier("used_furniture.conn") DbConnection conn) {
    this.conn = conn;
  }

  /**
   *
   * @param page
   * @param callerUrl the value of callerUrl
   */
  public void addPageVisit(String page, String callerUrl) {
    ZonedDateTime datetime = ZonedDateTime.now(ZoneId.of("UTC"));
    String datetimeText = getDateTimeText(datetime);
    String statement = "insert into users.pagevisit (datetime_utc, page, caller_ip) values \n"
            + String.format("(%s, '%s', '%s')\n", datetimeText, page, callerUrl);
    this.conn.executeStatement(statement);
  }

  /**
   *
   * @param datetime
   * @return
   */
  public String getDateTimeText(ZonedDateTime datetime) {
    String javaFormat = "yyyy/MM/dd HH:mm:ss";
    String postgresTimeFormat = "yyyy/mm/dd HH24:mi:ss";
    String datetimetext = formatUtc(datetime, javaFormat);
    String result = String.format("to_timestamp('%s', '%s')", //
            datetimetext, postgresTimeFormat);
    return result;
  }

  public List<PageVisitCount> getPageVisitCountsByDay() {
    String query = "select\n"
            + "	count(*) as count, \n"
            + " max(datetime_utc) as max_datetime_utc, "
            + " min(datetime_utc) as min_datetime_utc, "
            + "	caller_ip\n"
            + "from users.pagevisit\n"
            + "group by caller_ip\n"
            + "order by max_datetime_utc desc, count desc";
    ZoneId zoneID = ZoneId.of("UTC"); 
    List<PageVisitCount> result = this.conn.executeQuery(query, rs->{
      int count = RmDbUtils.intValue(rs, "count");
      
      ZonedDateTime maxDateTimeUtc = RmDbUtils.getZonedDateTime(rs, "max_datetime_utc", zoneID); 
      ZonedDateTime minDateTimeUtc = RmDbUtils.getZonedDateTime(rs, "min_datetime_utc", zoneID); 
      String callerIp = RmDbUtils.stringValue(rs, "caller_ip"); 
      return new PageVisitCount(callerIp, count, maxDateTimeUtc, minDateTimeUtc);
    });
    return result;
  }

}
