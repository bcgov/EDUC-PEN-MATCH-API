package ca.bc.gov.educ.api.penmatch.messaging.stan;

import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The type Pub sub.
 */
@Slf4j
public abstract class PubSub {

  /**
   * Close.
   *
   * @param connection the connection
   */
  protected void close(final StreamingConnection connection) {
    if (connection != null) {
      log.info("closing stan connection...");
      try {
        connection.close();
      } catch (final IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
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

  /**
   * This method will keep retrying for a connection.
   *
   * @param connectionFactory the connection factory
   * @return the streaming connection
   */
  protected StreamingConnection connectionLostHandler(final StreamingConnectionFactory connectionFactory) {
    StreamingConnection streamingConnection = null;
    int numOfRetries = 1;
    while (true) {
      try {
        log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
        streamingConnection = connectionFactory.createConnection();
        log.info("successfully reconnected after {} attempts", numOfRetries);
        break;
      } catch (final IOException ex) {
        this.backOff(numOfRetries, ex);
      } catch (final InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        this.backOff(numOfRetries, interruptedException);
      }
    }
    return streamingConnection;
  }
}
