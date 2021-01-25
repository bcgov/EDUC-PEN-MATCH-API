package ca.bc.gov.educ.api.penmatch.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;
import java.util.concurrent.Executor;

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
    return new EnhancedQueueExecutor.Builder()
        .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("message-subscriber-%d").build())
        .setCorePoolSize(20).setMaximumPoolSize(20).setKeepAliveTime(Duration.ofSeconds(60)).build();
  }

}
