package ca.bc.gov.educ.api.penmatch.messaging.stan;

import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.service.v1.events.STANEventHandlerService;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber implements Closeable {
  /**
   * The Connection factory.
   */
  private final StreamingConnectionFactory connectionFactory;
  /**
   * The Stan event handler service.
   */
  private final STANEventHandlerService stanEventHandlerService;
  /**
   * The Connection.
   */
  private StreamingConnection connection;

  /**
   * Instantiates a new Subscriber.
   *
   * @param applicationProperties   the application properties
   * @param natsConnection          the nats connection
   * @param stanEventHandlerService the stan event handler service
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  @Autowired
  public Subscriber(ApplicationProperties applicationProperties, NatsConnection natsConnection, STANEventHandlerService stanEventHandlerService) throws IOException, InterruptedException {
    this.stanEventHandlerService = stanEventHandlerService;
    Options options = new Options.Builder()
        .natsUrl(applicationProperties.getStanUrl())
        .clusterId(applicationProperties.getStanCluster())
        .connectionLostHandler(this::connectionLostHandler)
        .natsConn(natsConnection.getNatsCon())
        .maxPingsOut(30)
        .pingInterval(Duration.ofSeconds(2))
        .clientId("pen-match-api-subscriber" + UUID.randomUUID().toString()).build();
    connectionFactory = new StreamingConnectionFactory(options);
    connection = connectionFactory.createConnection();
  }


  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   *
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  @PostConstruct
  public void subscribe() throws InterruptedException, TimeoutException, IOException {
    SubscriptionOptions options = new SubscriptionOptions.Builder()
        .durableName("pen-match-api-pen-match-event-consumer").build();
    connection.subscribe(PEN_MATCH_EVENTS_TOPIC.toString(), "pen-match-api-pen-match-event", this::onPenMatchEventsTopicMessage, options);
  }

  /**
   * This method will process the event message pushed into the PEN_MATCH_EVENTS_TOPIC.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onPenMatchEventsTopicMessage(Message message) {
    if (message != null) {
      try {
        String eventString = new String(message.getData());
        ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
        stanEventHandlerService.updateEventStatus(event);
        log.info("received event :: {} ", event);
      } catch (final Exception ex) {
        log.error("Exception ", ex);
      }
    }
  }


  /**
   * Retry subscription.
   */
  private void retrySubscription() {
    int numOfRetries = 0;
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * This method will keep retrying for a connection.
   *
   * @param streamingConnection the streaming connection
   * @param e                   the e
   */
  private void connectionLostHandler(StreamingConnection streamingConnection, Exception e) {
    if (e != null) {
      reconnect();
      retrySubscription();
    }
  }

  /**
   * Reconnect.
   */
  private void reconnect() {
    int numOfRetries = 1;
    while (true) {
      try {
        log.trace("retrying connection as connection was lost :: retrying ::" + numOfRetries++);
        connection = connectionFactory.createConnection();
        log.info("successfully reconnected after {} attempts", numOfRetries);
        break;
      } catch (IOException ex) {
        backOff(numOfRetries, ex);
      } catch (InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        backOff(numOfRetries, interruptedException);
      }
    }
  }

  /**
   * Back off.
   *
   * @param numOfRetries the num of retries
   * @param ex           the ex
   */
  private void backOff(int numOfRetries, Exception ex) {
    log.error("exception occurred", ex);
    try {
      double sleepTime = (2 * numOfRetries);
      TimeUnit.SECONDS.sleep((long) sleepTime);
    } catch (InterruptedException exc) {
      log.error("exception occurred", exc);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close() {
    if (connection != null) {
      log.info("closing stan connection...");
      try {
        connection.close();
      } catch (IOException | TimeoutException | InterruptedException e) {
        log.error("error while closing stan connection...", e);
        Thread.currentThread().interrupt();
      }
      log.info("stan connection closed...");
    }
  }
}
