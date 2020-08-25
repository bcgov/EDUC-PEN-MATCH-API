package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.mappers.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.model.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.repository.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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

  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing.";
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay," +
    " just updating the db status so that it will be polled and sent back again.";
  public static final String EVENT_LOG = "event is :: {}";
  public static final String PAYLOAD_LOG = "Payload is :: ";
  @Getter(PRIVATE)
  private static final PenMatchStudentMapper penMatchStudentMapper = PenMatchStudentMapper.mapper;
  @Getter(PRIVATE)
  private final PENMatchEventRepository penMatchEventRepository;

  @Getter(PRIVATE)
  private final PenMatchService penMatchService;

  @Autowired
  public EventHandlerService(PENMatchEventRepository penMatchEventRepository, PenMatchService penMatchService) {
    this.penMatchEventRepository = penMatchEventRepository;
    this.penMatchService = penMatchService;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleEvent(Event event) {
    try {
      switch (event.getEventType()) {
        case PEN_MATCH_EVENT_OUTBOX_PROCESSED:
          log.info("received outbox processed event :: ");
          log.trace(PAYLOAD_LOG + event.getEventPayload());
          handlePenMatchEventOutboxProcessedEvent(event.getEventPayload());
          break;
        case PROCESS_PEN_MATCH:
          log.info("received PROCESS_PEN_MATCH event :: ");
          log.trace(PAYLOAD_LOG + event.getEventPayload());
          handleProcessPenMatchEvent(event);
          break;
        default:
          log.info("silently ignoring other events.");
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  private void handleProcessPenMatchEvent(@NonNull Event event) throws JsonProcessingException {
    var eventOptional = getPenMatchEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString()); //mandatory fields, should not be null.
    if (eventOptional.isEmpty()) {
      var result = getPenMatchService().matchStudent(penMatchStudentMapper.toPenMatchStudentDetails(JsonUtil.getJsonObjectFromString(PenMatchStudent.class,event.getEventPayload())));
      event.setEventOutcome(EventOutcome.PEN_MATCH_PROCESSED);
      event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
      eventOptional = Optional.of(createEventRecord(event));
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_LOG, event);
      var penMatchEvent = eventOptional.get();
      penMatchEvent.setEventStatus(DB_COMMITTED.toString()); // changing the status so that it will be polled and published again.
    }
    getPenMatchEventRepository().save(eventOptional.get());
  }


  private void handlePenMatchEventOutboxProcessedEvent(String eventId) {
    val eventOptional = getPenMatchEventRepository().findById(UUID.fromString(eventId));
    if (eventOptional.isPresent()) {
      val event = eventOptional.get();
      event.setEventStatus(MESSAGE_PUBLISHED.toString());
      getPenMatchEventRepository().save(event);
    }
  }


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
