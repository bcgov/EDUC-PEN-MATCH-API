package ca.bc.gov.educ.api.penmatch.service.events;

import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerDelegatorService {
  /**
   * The constant PAYLOAD_LOG.
   */
  public static final String PAYLOAD_LOG = "Payload is :: ";
  /**
   * The Event handler service.
   */
  @Getter
  private final EventHandlerService eventHandlerService;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   */
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
      if (event.getEventType() == EventType.PROCESS_PEN_MATCH) {
        log.info("received PROCESS_PEN_MATCH event for :: {}", event.getSagaId());
        log.debug(PAYLOAD_LOG + event.getEventPayload());
        getEventHandlerService().handleProcessPenMatchEvent(event);
      } else {
        log.info("silently ignoring other events {}", event);
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }


}
