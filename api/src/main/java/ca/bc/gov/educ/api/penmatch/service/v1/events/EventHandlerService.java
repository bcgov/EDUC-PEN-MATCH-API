package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PossibleMatchMapper;
import ca.bc.gov.educ.api.penmatch.service.v1.match.PenMatchService;
import ca.bc.gov.educ.api.penmatch.service.v1.match.PossibleMatchService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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

  /**
   * The Pen match service.
   */
  @Getter(PRIVATE)
  private final PossibleMatchService possibleMatchService;

  /**
   * The Ob mapper.
   */
  private final ObjectMapper obMapper = new ObjectMapper();

  /**
   * Instantiates a new Event handler service.
   *
   * @param penMatchService      the pen match service
   * @param possibleMatchService the possible match service
   */
  public EventHandlerService(PenMatchService penMatchService, PossibleMatchService possibleMatchService) {
    this.penMatchService = penMatchService;
    this.possibleMatchService = possibleMatchService;
  }

  /**
   * Handle process pen match event.
   * return response as soon as processing is done.
   *
   * @param event the event
   * @return the byte [ ]
   * @throws IOException the io exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleProcessPenMatchEvent(@NonNull Event event) throws IOException {
    var result = getPenMatchService().matchStudent(penMatchStudentMapper.toPenMatchStudentDetails(JsonUtil.getJsonObjectFromString(PenMatchStudent.class, event.getEventPayload())), event.getSagaId());
    log.info("PEN Match Result for event :: {} , is:: {} ", event, result);
    event.setEventOutcome(EventOutcome.PEN_MATCH_PROCESSED);
    event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
    Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.PEN_MATCH_PROCESSED)
        .eventPayload(JsonUtil.getJsonStringFromObject(result)).build();
    return obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle add possible match event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleAddPossibleMatchEvent(@NonNull final Event event) throws JsonProcessingException {
    JavaType type = obMapper.getTypeFactory().
        constructCollectionType(List.class, PossibleMatch.class);

    List<PossibleMatch> possibleMatches = obMapper.readValue(event.getEventPayload(), type);
    var result = getPossibleMatchService().savePossibleMatches(possibleMatches
        .stream().map(PossibleMatchMapper.mapper::toModel).collect(Collectors.toList()))
        .stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList());
    Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_ADDED)
        .eventPayload(JsonUtil.getJsonStringFromObject(result)).build();
    return obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle get possible match event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleGetPossibleMatchEvent(@NonNull final Event event) throws JsonProcessingException {
    final List<PossibleMatch> response;
    var result = getPossibleMatchService().getPossibleMatches(UUID.fromString(event.getEventPayload()));
    EventOutcome eventOutcome;
    if (result.isEmpty()) {
      response = new ArrayList<>();
      eventOutcome = EventOutcome.POSSIBLE_MATCH_NOT_FOUND;
    } else {
      eventOutcome = EventOutcome.POSSIBLE_MATCH_FOUND;
      response = result.stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList());
    }
    Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(eventOutcome)
        .eventPayload(JsonUtil.getJsonStringFromObject(response)).build();
    return obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle delete possible match event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public byte[] handleDeletePossibleMatchEvent(@NonNull final Event event) throws JsonProcessingException {
    final List<Map<String, UUID>> payload = obMapper.readValue(event.getEventPayload(), new TypeReference<>() {
    });
    if (!payload.isEmpty()) {
      payload.forEach(item -> getPossibleMatchService().deletePossibleMatches(item.get("studentID"), item.get("matchedStudentID")));
    }

    Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_DELETED)
        .eventPayload(EventOutcome.POSSIBLE_MATCH_DELETED.toString()).build();
    return obMapper.writeValueAsBytes(newEvent);
  }
}
