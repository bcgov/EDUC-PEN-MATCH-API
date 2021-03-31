package ca.bc.gov.educ.api.penmatch.messaging.stan;

import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class StanConnection implements Closeable {

  private StreamingConnectionFactory connectionFactory;

  @Getter
  private StreamingConnection connection;

  /**
   * Instantiates a new Publisher.
   *
   * @param applicationProperties the application properties
   * @param natsConnection        the nats connection
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public StanConnection(final ApplicationProperties applicationProperties, final NatsConnection natsConnection) throws IOException, InterruptedException {
    if (applicationProperties.getIsSTANEnabled() != null && applicationProperties.getIsSTANEnabled()) {
      final Options options = new Options.Builder()
          .clusterId(applicationProperties.getStanCluster())
          .connectionLostHandler(this::connectionLostHandler)
          .natsConn(natsConnection.getNatsCon())
          .traceConnection()
          .maxPingsOut(30)
          .pingInterval(Duration.ofSeconds(2))
          .clientId("pen-match-api-" + UUID.randomUUID().toString()).build();
      this.connectionFactory = new StreamingConnectionFactory(options);
      this.connection = this.connectionFactory.createConnection();
    }
  }


  /**
   * This method will keep retrying for a connection.
   */
  private void connectionLostHandler(final StreamingConnection streamingConnection, final Exception exception) {
    int numOfRetries = 1;
    while (true) {
      try {
        log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
        this.connection = this.connectionFactory.createConnection();
        log.info("successfully reconnected after {} attempts", numOfRetries);
        break;
      } catch (final IOException ex) {
        this.backOff(numOfRetries, ex);
      } catch (final InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        this.backOff(numOfRetries, interruptedException);
      }
    }
  }

  /**
   * Back off.
   *
   * @param numOfRetries the num of retries
   * @param ex           the ex
   */
  protected void backOff(final int numOfRetries, final Exception ex) {
    log.error("exception occurred", ex);
    try {
      final double sleepTime = (2 * numOfRetries);
      TimeUnit.SECONDS.sleep((long) sleepTime);
    } catch (final InterruptedException exc) {
      log.error("exception occurred", exc);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close() {
    if (this.connection != null) {
      log.info("closing stan connection...");
      try {
        this.connection.close();
      } catch (final IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
    }
  }
}
