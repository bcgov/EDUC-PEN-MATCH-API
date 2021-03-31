package ca.bc.gov.educ.api.penmatch.messaging.stan;

import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.service.v1.events.STANEventHandlerService;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.streaming.Message;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@Slf4j
public class Subscriber {

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
   * @param stanConnection          the stan connection
   * @param stanEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final ApplicationProperties applicationProperties, final StanConnection stanConnection,
                    final STANEventHandlerService stanEventHandlerService) {
    this.stanEventHandlerService = stanEventHandlerService;
    if (applicationProperties.getIsSTANEnabled() != null && applicationProperties.getIsSTANEnabled()) {
      this.connection = stanConnection.getConnection();
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

}
