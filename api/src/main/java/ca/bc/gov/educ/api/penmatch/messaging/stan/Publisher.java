package ca.bc.gov.educ.api.penmatch.messaging.stan;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import io.nats.streaming.StreamingConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_EVENTS_TOPIC;

/**
 * The type Publisher.
 */
@Component
@Slf4j
public class Publisher {

  private StreamingConnection connection;


  @Autowired
  public Publisher(final ApplicationProperties applicationProperties, final StanConnection stanConnection) {
    if (applicationProperties.getIsSTANEnabled() != null && applicationProperties.getIsSTANEnabled()) {
      this.connection = stanConnection.getConnection();
    }
  }

  /**
   * Dispatch choreography event.
   *
   * @param event the event
   */
  public void dispatchChoreographyEvent(@NonNull final PENMatchEvent event) {
    if (event.getEventId() != null) {
      final ChoreographedEvent choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(event.getEventType()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome()));
      choreographedEvent.setEventPayload(event.getEventPayload());
      choreographedEvent.setEventID(event.getEventId().toString());
      choreographedEvent.setCreateUser(event.getCreateUser());
      choreographedEvent.setUpdateUser(event.getUpdateUser());
      try {
        log.info("Broadcasting event :: {}", choreographedEvent);
        this.connection.publish(PEN_MATCH_EVENTS_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(choreographedEvent));
      } catch (final IOException | TimeoutException e) {
        log.error("exception while broadcasting message to STAN", e);
      } catch (final InterruptedException e) {
        log.error("exception while broadcasting message to STAN", e);
        Thread.currentThread().interrupt();
      }
    }
  }

}
