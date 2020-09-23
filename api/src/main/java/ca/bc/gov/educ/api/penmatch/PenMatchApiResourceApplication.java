package ca.bc.gov.educ.api.penmatch;
import jodd.util.ThreadFactoryBuilder;
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCaching
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1s")
@EnableRetry
@EnableAsync
public class PenMatchApiResourceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PenMatchApiResourceApplication.class, args);
  }

  @Configuration
  static
  class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
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
  }

  /**
   * Lock provider For distributed lock, to avoid multiple pods executing the same scheduled task.
   *
   * @param jdbcTemplate       the jdbc template
   * @param transactionManager the transaction manager
   * @return the lock provider
   */
  @Bean
  @Autowired
  public LockProvider lockProvider(final JdbcTemplate jdbcTemplate, final PlatformTransactionManager transactionManager) {
    return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager, "PEN_MATCH_SHEDLOCK");
  }

  @Bean(name = "subscriberExecutor")
  public Executor threadPoolTaskExecutor() {
    ThreadFactory namedThreadFactory =
        new ThreadFactoryBuilder().withNameFormat("message-subscriber-%d").get();
    return Executors.newFixedThreadPool(18, namedThreadFactory);
  }
  @Bean(name = "controllerExecutor")
  public Executor controllerTaskExecutor() {
    ThreadFactory namedThreadFactory =
        new ThreadFactoryBuilder().withNameFormat("controller-%d").get();
    return Executors.newFixedThreadPool(8, namedThreadFactory);
  }
}
