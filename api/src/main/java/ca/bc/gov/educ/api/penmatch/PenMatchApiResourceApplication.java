package ca.bc.gov.educ.api.penmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The type Pen match api resource application.
 */
@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCaching
@EnableScheduling
@EnableRetry
public class PenMatchApiResourceApplication {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(PenMatchApiResourceApplication.class, args);
  }


  /**
   * The type Web security configuration.
   */
  @Configuration
  static
  class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    /**
     * Instantiates a new Web security configuration.
     */
    public WebSecurityConfiguration() {
      super();
      SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    public void configure(WebSecurity web) {
      web.ignoring().antMatchers("/v3/api-docs/**",
          "/actuator/health", "/actuator/prometheus",
          "/swagger-ui/**", "/health");
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .anyRequest().authenticated().and()
          .oauth2ResourceServer().jwt();
    }
  }
}
