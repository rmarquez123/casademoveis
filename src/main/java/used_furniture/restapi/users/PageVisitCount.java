package used_furniture.restapi.users;

import java.time.ZonedDateTime;

/**
 *
 * @author rmarq
 */
public class PageVisitCount {

  public final String callerIp;
  public final int count;
  public final ZonedDateTime maxDateTimeUtc;
  public final ZonedDateTime minDateTimeUtc;
  
  /**
   * 
   * @param callerIp
   * @param count
   * @param maxDateTimeUtc
   * @param minDateTimeUtc 
   */
  PageVisitCount(String callerIp, int count, ZonedDateTime maxDateTimeUtc, ZonedDateTime minDateTimeUtc) {
    this.callerIp = callerIp;
    this.count = count;
    this.maxDateTimeUtc = maxDateTimeUtc;
    this.minDateTimeUtc = minDateTimeUtc;
  }
  
}
