package ca.bc.gov.educ.api.penmatch;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The type Pen match api resource application.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
@EnableSchedulerLock(defaultLockAtMostFor = "1s")
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
  @EnableMethodSecurity
  static
  class WebSecurityConfiguration {
    /**
     * Instantiates a new Web security configuration.
     */
    public WebSecurityConfiguration() {
      super();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/v3/api-docs/**",
                  "/actuator/health", "/actuator/prometheus", "/actuator/**",
                  "/swagger-ui/**").permitAll()
              .anyRequest().authenticated()
          )
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
      return http.build();
    }
  }

  /**
   * Lock provider lock provider.
   *
   * @param jdbcTemplate       the jdbc template
   * @param transactionManager the transaction manager
   * @return the lock provider
   */
  @Bean
  public LockProvider lockProvider(@Autowired JdbcTemplate jdbcTemplate, @Autowired PlatformTransactionManager transactionManager) {
    return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager, "PEN_MATCH_SHEDLOCK");
  }
}
