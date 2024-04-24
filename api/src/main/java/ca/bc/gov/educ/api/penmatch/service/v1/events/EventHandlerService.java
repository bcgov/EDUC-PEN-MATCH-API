package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PossibleMatchMapper;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  public EventHandlerService(final PenMatchService penMatchService, final PossibleMatchService possibleMatchService) {
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
  public byte[] handleProcessPenMatchEvent(@NonNull final Event event) throws IOException {
    final var result = this.getPenMatchService().matchStudent(penMatchStudentMapper.toPenMatchStudentDetails(JsonUtil.getJsonObjectFromString(PenMatchStudent.class, event.getEventPayload())), event.getSagaId());
    log.debug("PEN Match Result for event :: {} , is:: {} ", event, result);
    event.setEventOutcome(EventOutcome.PEN_MATCH_PROCESSED);
    event.setEventPayload(JsonUtil.getJsonStringFromObject(result));
    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.PEN_MATCH_PROCESSED)
        .eventPayload(JsonUtil.getJsonStringFromObject(result)).build();
    return this.obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle add possible match event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public Pair<byte[], Optional<PENMatchEvent>> handleAddPossibleMatchEvent(@NonNull final Event event) throws JsonProcessingException {
    final JavaType type = this.obMapper.getTypeFactory().
        constructCollectionType(List.class, PossibleMatch.class);

    final List<PossibleMatch> possibleMatches = this.obMapper.readValue(event.getEventPayload(), type);
    final var pair = this.getPossibleMatchService().savePossibleMatches(possibleMatches
        .stream().map(PossibleMatchMapper.mapper::toModel).collect(Collectors.toList()));
    final var savedItems = pair.getLeft().stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList());
    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_ADDED)
        .eventPayload(JsonUtil.getJsonStringFromObject(savedItems)).build();
    return Pair.of(this.obMapper.writeValueAsBytes(newEvent), pair.getRight());
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
    final var result = this.getPossibleMatchService().getPossibleMatches(UUID.fromString(event.getEventPayload()));
    final EventOutcome eventOutcome;
    if (result.isEmpty()) {
      response = new ArrayList<>();
      eventOutcome = EventOutcome.POSSIBLE_MATCH_NOT_FOUND;
    } else {
      eventOutcome = EventOutcome.POSSIBLE_MATCH_FOUND;
      response = result.stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList());
    }
    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(eventOutcome)
        .eventPayload(JsonUtil.getJsonStringFromObject(response)).build();
    return this.obMapper.writeValueAsBytes(newEvent);
  }

  /**
   * Handle delete possible match event byte [ ].
   *
   * @param event the event
   * @return the byte [ ]
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = REQUIRES_NEW)
  public Pair<byte[], Optional<PENMatchEvent>> handleDeletePossibleMatchEvent(@NonNull final Event event) throws JsonProcessingException {
    Optional<PENMatchEvent> penMatchEventOptional = Optional.empty();
    final List<PossibleMatch> payload = this.obMapper.readValue(event.getEventPayload(), new TypeReference<>() {
    });
    if (!payload.isEmpty()) {
      penMatchEventOptional = this.getPossibleMatchService().deletePossibleMatches(payload);
    }

    final Event newEvent = Event.builder()
        .sagaId(event.getSagaId())
        .eventType(event.getEventType())
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_DELETED)
        .eventPayload(EventOutcome.POSSIBLE_MATCH_DELETED.toString()).build();
    return Pair.of(this.obMapper.writeValueAsBytes(newEvent), penMatchEventOptional);
  }
}
