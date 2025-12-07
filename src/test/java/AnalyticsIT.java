import common.db.DbConnection;
import common.http.RmHttpReader;
import java.time.Duration;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import used_furniture.restapi.users.PageVisitCount;
import used_furniture.restapi.users.UsersService;

/**
 *
 * @author rmarq
 */
public class AnalyticsIT extends BaseIT{
    
  /**
   * 
   */
  @Test
  public void test() {
    DbConnection conn = this.getDbConnection();
    UsersService service = new UsersService(conn);
    List<PageVisitCount> visits = service.getPageVisitCountsByDay();
    int count = 0; 
    for (PageVisitCount visit : visits) {
      String country = this.getCountry(visit.callerIp);
      if (!country.equals("BR")) {
        continue;
      }
      count++; 
      Duration duration = Duration.between(visit.maxDateTimeUtc, visit.minDateTimeUtc);
      String text = String.format("%d: \t%s - \t%s - \t%s - \t%s - \t%s - \t%d", 
              count, visit.callerIp, country, visit.maxDateTimeUtc, visit.minDateTimeUtc, duration, visit.count);
              
      System.out.println(text);
    }
  }
    
  /**
   * 
   * @param callerIp
   * @return 
   */
  private String getCountry(String callerIp) {
    try {
      String url = "https://api.iplocation.net";
      JSONObject jsonObject = new RmHttpReader.Builder(url) //
              .setRequestParam("ip", callerIp) //
              .readJsonObject();//
      String result = jsonObject.getString("country_code2");
      return result;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
  
}
