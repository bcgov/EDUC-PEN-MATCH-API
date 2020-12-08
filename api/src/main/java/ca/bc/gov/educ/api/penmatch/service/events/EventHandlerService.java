package ca.bc.gov.educ.api.penmatch.service.events;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.mappers.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.messaging.MessagePublisher;
import ca.bc.gov.educ.api.penmatch.service.match.PenMatchService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  /**
   * The constant penMatchStudentMapper.
   */
  @Getter(PRIVATE)
  private static final PenMatchStudentMapper penMatchStudentMapper = PenMatchStudentMapper.mapper;

  /**
   * The Pen match service.
   */
  @Getter(PRIVATE)
  private final PenMatchService penMatchService;

  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  /**
   * Instantiates a new Event handler service.
   *
   * @param penMatchService         the pen match service
   * @param messagePublisher        the message publisher
   */
  public EventHandlerService(PenMatchService penMatchService, MessagePublisher messagePublisher) {
    this.penMatchService = penMatchService;
    this.messagePublisher = messagePublisher;
  }

  /**
   * Handle process pen match event.
   * return response as soon as processing is done.
   * @param event the event
   */
  @Async("subscriberExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleProcessPenMatchEvent(@NonNull Event event) {
    try {
      var obMapper = new ObjectMapper();
      var result = getPenMatchService().matchStudent(penMatchStudentMapper.toPenMatchStudentDetails(JsonUtil.getJsonObjectFromString(PenMatchStudent.class, event.getEventPayload())));
      log.debug("PEN Match Result for saga id :: {} , is:: {} ", event.getSagaId(), result);
      event.setEventOutcome(EventOutcome.PEN_MATCH_PROCESSED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
      Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.PEN_MATCH_PROCESSED)
        .eventPayload(JsonUtil.getJsonStringFromObject(result)).build();
      getMessagePublisher().dispatchMessage(event.getReplyTo(), obMapper.writeValueAsBytes(newEvent));
    } catch (final Exception jsonProcessingException) {
      log.error("JSON processing exception for :: {} , {}", event.getSagaId(), jsonProcessingException);
    }
  }

}
