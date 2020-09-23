package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.struct.Event;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {
  public static final String PAYLOAD_LOG = "Payload is :: ";
  @Getter
  private final EventHandlerService eventHandlerService;

  @Autowired
  public EventHandlerDelegatorService(EventHandlerService eventHandlerService) {
    this.eventHandlerService = eventHandlerService;
  }

  /**
   * Handle event.
   *
   * @param event the event
   */

  public void handleEvent(Event event) {
    try {
      switch (event.getEventType()) {
        case PEN_MATCH_EVENT_OUTBOX_PROCESSED:
          log.info("received outbox processed event :: ");
          log.info(PAYLOAD_LOG + event.getEventPayload());
          getEventHandlerService().handlePenMatchEventOutboxProcessedEvent(event.getEventPayload());
          break;
        case PROCESS_PEN_MATCH:
          log.info("received PROCESS_PEN_MATCH event :: ");
          log.trace(PAYLOAD_LOG + event.getEventPayload());
          getEventHandlerService().handleProcessPenMatchEvent(event);
          break;
        default:
          log.info("silently ignoring other events.");
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }


}
