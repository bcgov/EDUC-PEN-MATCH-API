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
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber extends PubSub implements Closeable {
  /**
   * The Connection factory.
   */
  private StreamingConnectionFactory connectionFactory;
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
  public Subscriber(final ApplicationProperties applicationProperties, final NatsConnection natsConnection, final STANEventHandlerService stanEventHandlerService) throws IOException, InterruptedException {
    this.stanEventHandlerService = stanEventHandlerService;
    if (applicationProperties.getIsSTANEnabled() != null && applicationProperties.getIsSTANEnabled()) {
      final Options options = new Options.Builder()
          .clusterId(applicationProperties.getStanCluster())
          .connectionLostHandler(this::connectionLostHandler)
          .natsConn(natsConnection.getNatsCon())
          .maxPingsOut(30)
          .pingInterval(Duration.ofSeconds(2))
          .clientId("pen-match-api-subscriber" + UUID.randomUUID().toString()).build();
      this.connectionFactory = new StreamingConnectionFactory(options);
      this.connection = this.connectionFactory.createConnection();
    }
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
    if (this.connection != null) {
      final SubscriptionOptions options = new SubscriptionOptions.Builder()
          .durableName("pen-match-api-pen-match-event-consumer").build();
      this.connection.subscribe(PEN_MATCH_EVENTS_TOPIC.toString(), "pen-match-api-pen-match-event", this::onPenMatchEventsTopicMessage, options);
    }
  }

  /**
   * This method will process the event message pushed into the PEN_MATCH_EVENTS_TOPIC.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onPenMatchEventsTopicMessage(final Message message) {
    if (message != null) {
      try {
        final String eventString = new String(message.getData());
        final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
        this.stanEventHandlerService.updateEventStatus(event);
        log.info("received event :: {} ", event);
      } catch (final Exception ex) {
        log.error("Exception ", ex);
      }
    }
  }


  /**
   * This method will keep retrying for a connection.
   *
   * @param streamingConnection the streaming connection
   * @param e                   the e
   */
  private void connectionLostHandler(final StreamingConnection streamingConnection, final Exception e) {
    this.connection = super.connectionLostHandler(this.connectionFactory);
    this.retrySubscription();
  }

  private void retrySubscription() {
    int numOfRetries = 0;
    while (true) {
      try {
        log.trace("retrying subscription as connection was lost :: retrying ::" + numOfRetries++);
        this.subscribe();
        log.info("successfully resubscribed after {} attempts", numOfRetries);
        break;
      } catch (final InterruptedException | TimeoutException | IOException exception) {
        log.error("exception occurred while retrying subscription", exception);
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void close() {
    super.close(this.connection);
  }
}
