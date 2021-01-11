package ca.bc.gov.educ.api.penmatch.config;

import jodd.util.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The type Async configuration.
 */
@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {
  /**
   * Thread pool task executor executor.
   *
   * @return the executor
   */
  @Bean(name = "subscriberExecutor")
  public Executor threadPoolTaskExecutor() {
    ThreadFactory namedThreadFactory =
        new ThreadFactoryBuilder().withNameFormat("message-subscriber-%d").get();
    return Executors.newFixedThreadPool(50, namedThreadFactory);
  }

}
