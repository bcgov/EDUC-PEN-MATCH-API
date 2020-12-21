package ca.bc.gov.educ.api.penmatch.service.events;

import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.messaging.MessagePublisher;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static lombok.AccessLevel.PRIVATE;

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
   * The Message publisher.
   */
  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   * @param messagePublisher    the message publisher
   */
  @Autowired
  public EventHandlerDelegatorService(EventHandlerService eventHandlerService, MessagePublisher messagePublisher) {
    this.eventHandlerService = eventHandlerService;
    this.messagePublisher = messagePublisher;
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  @Async("subscriberExecutor")
  public void handleEvent(Event event, Message message) {
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      if (event.getEventType() == EventType.PROCESS_PEN_MATCH) {
        log.info("received PROCESS_PEN_MATCH event for :: {}", event.getSagaId());
        log.debug(PAYLOAD_LOG + event.getEventPayload());
        var response = getEventHandlerService().handleProcessPenMatchEvent(event);
        if(isSynchronous){ // this is for synchronous request/reply pattern.
          getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
        }
        else{ // this is for async.
          getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
        }
      } else {
        log.info("silently ignoring other event :: {}", event);
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }


}
