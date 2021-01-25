package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.messaging.MessagePublisher;
import ca.bc.gov.educ.api.penmatch.messaging.stan.Publisher;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
   * The Publisher.
   */
  @Getter(PRIVATE)
  private final Publisher publisher; // STAN publisher for choreography

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param eventHandlerService the event handler service
   * @param messagePublisher    the message publisher
   * @param publisher           the publisher
   */
  @Autowired
  public EventHandlerDelegatorService(EventHandlerService eventHandlerService, MessagePublisher messagePublisher, Publisher publisher) {
    this.eventHandlerService = eventHandlerService;
    this.messagePublisher = messagePublisher;
    this.publisher = publisher;
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
    final Pair<byte[], Optional<PENMatchEvent>> pairedResult;
    byte[] response;
    try {
      switch (event.getEventType()) {
        case PROCESS_PEN_MATCH:
          log.info("received PROCESS_PEN_MATCH event for :: {}", event.getSagaId());
          log.debug(PAYLOAD_LOG + event.getEventPayload());
          response = getEventHandlerService().handleProcessPenMatchEvent(event);
          publishToNATS(event, message, isSynchronous, response);
          break;
        case ADD_POSSIBLE_MATCH:
          log.info("received ADD_POSSIBLE_MATCH event for :: {}", event.getSagaId());
          log.debug(PAYLOAD_LOG + event.getEventPayload());
          pairedResult = getEventHandlerService().handleAddPossibleMatchEvent(event);
          publishToNATS(event, message, isSynchronous, pairedResult.getLeft());
          pairedResult.getRight().ifPresent(this::publishToSTAN);
          break;
        case GET_POSSIBLE_MATCH:
          log.info("received GET_POSSIBLE_MATCH event for :: {}", event.getSagaId());
          log.debug(PAYLOAD_LOG + event.getEventPayload());
          response = getEventHandlerService().handleGetPossibleMatchEvent(event);
          publishToNATS(event, message, isSynchronous, response);
          break;
        case DELETE_POSSIBLE_MATCH:
          log.info("received DELETE_POSSIBLE_MATCH event for :: {}", event.getSagaId());
          log.debug(PAYLOAD_LOG + event.getEventPayload());
          var pair = getEventHandlerService().handleDeletePossibleMatchEvent(event);
          publishToNATS(event, message, isSynchronous, pair.getLeft());
          if (!pair.getRight().isEmpty()) {
            pair.getRight().forEach(this::publishToSTAN);
          }
          break;
        default:
          log.info("silently ignoring other event :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  /**
   * Publish to stan.
   *
   * @param event the event
   */
  private void publishToSTAN(@NonNull final PENMatchEvent event) {
    publisher.dispatchChoreographyEvent(event);
  }


  /**
   * Publish to nats.
   *
   * @param event         the event
   * @param message       the message
   * @param isSynchronous the is synchronous
   * @param response      the response
   */
  private void publishToNATS(Event event, Message message, boolean isSynchronous, byte[] response) {
    if (isSynchronous) { // this is for synchronous request/reply pattern.
      getMessagePublisher().dispatchMessage(message.getReplyTo(), response);
    } else { // this is for async.
      getMessagePublisher().dispatchMessage(event.getReplyTo(), response);
    }
  }


}
