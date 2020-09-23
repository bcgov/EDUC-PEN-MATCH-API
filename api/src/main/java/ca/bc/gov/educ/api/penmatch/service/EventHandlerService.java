package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.mappers.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.model.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.repository.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.MESSAGE_PUBLISHED;
import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventHandlerService {

  /**
   * The constant NO_RECORD_SAGA_ID_EVENT_TYPE.
   */
  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing. {} {}";
  /**
   * The constant RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE.
   */
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay, {} {}" +
      " just updating the db status so that it will be polled and sent back again.";
  /**
   * The constant EVENT_LOG.
   */
  public static final String EVENT_LOG = "event is :: {}";
  /**
   * The constant PAYLOAD_LOG.
   */
  public static final String PAYLOAD_LOG = "Payload is :: ";
  /**
   * The constant penMatchStudentMapper.
   */
  @Getter(PRIVATE)
  private static final PenMatchStudentMapper penMatchStudentMapper = PenMatchStudentMapper.mapper;
  /**
   * The Pen match event repository.
   */
  @Getter(PRIVATE)
  private final PENMatchEventRepository penMatchEventRepository;

  /**
   * The Pen match service.
   */
  @Getter(PRIVATE)
  private final PenMatchService penMatchService;

  public EventHandlerService(PENMatchEventRepository penMatchEventRepository, PenMatchService penMatchService) {
    this.penMatchEventRepository = penMatchEventRepository;
    this.penMatchService = penMatchService;
  }

  /**
   * Handle process pen match event.
   *
   * @param event the event
   * @throws JsonProcessingException the json processing exception
   */
  @Async("subscriberExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleProcessPenMatchEvent(@NonNull Event event) throws JsonProcessingException {
    var eventOptional = getPenMatchEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString()); //mandatory fields, should not be null.
    if (eventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE, event.getSagaId(), event.getEventType());
      var result = getPenMatchService().matchStudent(penMatchStudentMapper.toPenMatchStudentDetails(JsonUtil.getJsonObjectFromString(PenMatchStudent.class, event.getEventPayload())));
      event.setEventOutcome(EventOutcome.PEN_MATCH_PROCESSED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
      eventOptional = Optional.of(createEventRecord(event));
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE, event.getSagaId(), event.getEventType());
      log.trace(EVENT_LOG, event);
      var penMatchEvent = eventOptional.get();
      penMatchEvent.setEventStatus(DB_COMMITTED.toString()); // changing the status so that it will be polled and published again.
    }
    getPenMatchEventRepository().save(eventOptional.get());
  }


  /**
   * Handle pen match event outbox processed event.
   *
   * @param eventId the event id
   */
  public void handlePenMatchEventOutboxProcessedEvent(String eventId) {
    val eventOptional = getPenMatchEventRepository().findById(UUID.fromString(eventId));
    if (eventOptional.isPresent()) {
      val event = eventOptional.get();
      event.setEventStatus(MESSAGE_PUBLISHED.toString());
      getPenMatchEventRepository().save(event);
    }else {
      log.error("Did not find anything for :: {}", eventId);
    }
  }


  /**
   * Create event record pen match event.
   *
   * @param event the event
   * @return the pen match event
   */
  private PENMatchEvent createEventRecord(Event event) {
    return PENMatchEvent.builder()
        .createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now())
        .createUser(event.getEventType().toString()) //need to discuss what to put here.
        .updateUser(event.getEventType().toString())
        .eventPayload(event.getEventPayload())
        .eventType(event.getEventType().toString())
        .sagaId(event.getSagaId())
        .eventStatus(DB_COMMITTED.toString())
        .eventOutcome(event.getEventOutcome().toString())
        .replyChannel(event.getReplyTo())
        .build();
  }
}
