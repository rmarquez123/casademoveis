package used_furniture.restapi;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *
 * @author rmarq
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and()
            .csrf().disable() // Disable CSRF for testing (use with caution in production)
            .authorizeRequests()
            .anyRequest().permitAll(); // Allow all requests for now
  }
  
//  @Override
//  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//    // In-memory authentication example (you can replace this with your own auth logic)
//    auth.inMemoryAuthentication()
//            .withUser("user").password("{noop}password").roles("USER");
//  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "https://admin.casademoveisusados.com",
            "https://casademoveisusados.com"
    ));
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true);
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
