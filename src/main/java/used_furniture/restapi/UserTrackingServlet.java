package used_furniture.restapi;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import used_furniture.restapi.users.UsersService;

/**
 *
 * @author rmarq
 */
@Controller
public class UserTrackingServlet {

  @Autowired
  private UsersService usersService;

  /**
   *
   * @param page
   * @param request
   */
  @RequestMapping(path = "/api/track")
  @ResponseBody
  public void tracking(@RequestParam(name = "page") String page, HttpServletRequest request) {
    String callerUrl = getClientIp(request);
    this.usersService.addPageVisit(page, callerUrl);
  }

  /**
   *
   * @param request
   * @return
   */
  private String getClientIp(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("X-Real-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }

    // If the header contains multiple IPs (e.g., "client, proxy1, proxy2"), take the first one
    if (ipAddress != null && ipAddress.contains(",")) {
      ipAddress = ipAddress.split(",")[0].trim();
    }
    return ipAddress;
  }
}
