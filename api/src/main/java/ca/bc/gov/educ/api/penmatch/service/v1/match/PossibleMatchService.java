package ca.bc.gov.educ.api.penmatch.service.v1.match;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PossibleMatchMapper;
import ca.bc.gov.educ.api.penmatch.model.v1.MatchReasonCodeEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.model.v1.PossibleMatchEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchReasonCodeRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.PossibleMatchRepository;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.DB_COMMITTED;

/**
 * The type Possible match service.
 */
@Service
@Slf4j
public class PossibleMatchService {

  /**
   * The Possible match repository.
   */
  private final PossibleMatchRepository possibleMatchRepository;

  /**
   * The Match reason code repository.
   */
  private final MatchReasonCodeRepository matchReasonCodeRepository;


  /**
   * The Pen match event repository.
   */
  private final PENMatchEventRepository penMatchEventRepository;

  /**
   * Instantiates a new Possible match service.
   *
   * @param possibleMatchRepository   the possible match repository
   * @param matchReasonCodeRepository the match reason code repository
   * @param penMatchEventRepository   the pen match event repository
   */
  @Autowired
  public PossibleMatchService(PossibleMatchRepository possibleMatchRepository, MatchReasonCodeRepository matchReasonCodeRepository, PENMatchEventRepository penMatchEventRepository) {
    this.possibleMatchRepository = possibleMatchRepository;
    this.matchReasonCodeRepository = matchReasonCodeRepository;
    this.penMatchEventRepository = penMatchEventRepository;
  }

  /**
   * Save possible matches list.
   *
   * @param possibleMatchEntities the possible match entities
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public Pair<List<PossibleMatchEntity>, Optional<PENMatchEvent>> savePossibleMatches(List<PossibleMatchEntity> possibleMatchEntities) throws JsonProcessingException {
    var updatedList = populateTwoWayAssociationOfEntities(possibleMatchEntities);
    if (!updatedList.isEmpty()) {
      val persistedEntities = possibleMatchRepository.saveAll(updatedList);
      return Pair.of(persistedEntities, Optional.of(penMatchEventRepository.save(createPossibleMatchEvent(possibleMatchEntities.get(0).getCreateUser(), possibleMatchEntities.get(0).getUpdateUser(), JsonUtil.getJsonStringFromObject(persistedEntities.stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList())), EventType.ADD_POSSIBLE_MATCH, EventOutcome.POSSIBLE_MATCH_ADDED))));
    }
    return Pair.of(new ArrayList<>(), Optional.empty());
  }

  /**
   * Create possible match event pen match event.
   *
   * @param createUser   the create user
   * @param updateUser   the update user
   * @param jsonString   the json string
   * @param eventType    the event type
   * @param eventOutcome the event outcome
   * @return the pen match event
   */
  private PENMatchEvent createPossibleMatchEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome) {
    return PENMatchEvent.builder()
        .createDate(LocalDateTime.now())
        .updateDate(LocalDateTime.now())
        .createUser(createUser)
        .updateUser(updateUser)
        .eventPayload(jsonString)
        .eventType(eventType.toString())
        .eventStatus(DB_COMMITTED.toString())
        .eventOutcome(eventOutcome.toString())
        .build();

  }

  /**
   * Gets possible matches.
   *
   * @param studentID the student id
   * @return the possible matches
   */
  @Transactional(readOnly = true)
  public List<PossibleMatchEntity> getPossibleMatches(UUID studentID) {
    return possibleMatchRepository.findAllByStudentID(studentID);
  }

  /**
   * Delete possible matches.
   *
   * @param possibleMatches the possible matches
   * @return optional optional
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public Optional<PENMatchEvent> deletePossibleMatches(List<PossibleMatch> possibleMatches) throws JsonProcessingException {
    var possibleMatchesFound = new ArrayList<PossibleMatchEntity>();
    for (var possibleMatch : possibleMatches) {
      var possibleMatchFromStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(possibleMatch.getStudentID(), possibleMatch.getMatchedStudentID());
      var possibleMatchFromMatchedStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(possibleMatch.getMatchedStudentID(), possibleMatch.getStudentID());
      possibleMatchFromStudentOptional.ifPresent(possibleMatchesFound::add);
      possibleMatchFromMatchedStudentOptional.ifPresent(possibleMatchesFound::add);
    }

    if (!possibleMatchesFound.isEmpty()) {
      possibleMatchRepository.deleteAll(possibleMatchesFound);
      return Optional.of(penMatchEventRepository.save(createPossibleMatchEvent(possibleMatches.get(0).getCreateUser(), possibleMatches.get(0).getUpdateUser(), JsonUtil.getJsonStringFromObject(possibleMatchesFound.stream().map(PossibleMatchMapper.mapper::toStruct).collect(Collectors.toList())), EventType.DELETE_POSSIBLE_MATCH, EventOutcome.POSSIBLE_MATCH_DELETED)));
    }
    return Optional.empty();
  }


  /**
   * Gets all match reason codes.
   *
   * @return the all match reason codes
   */
  public List<MatchReasonCodeEntity> getAllMatchReasonCodes() {
    return matchReasonCodeRepository.findAll();
  }

  /**
   * Populate two way association of entities list.
   * this method will check in DB if association already exist it will ignore. this makes the call idempotent.
   *
   * @param possibleMatchEntities the possible match entities
   * @return the list
   */
  private List<PossibleMatchEntity> populateTwoWayAssociationOfEntities(List<PossibleMatchEntity> possibleMatchEntities) {
    List<PossibleMatchEntity> possibleMatchEntitiesList = new CopyOnWriteArrayList<>();
    for (var possibleMatch : possibleMatchEntities) {
      var possibleMatchFromStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(possibleMatch.getStudentID(), possibleMatch.getMatchedStudentID());
      var possibleMatchFromMatchedStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(possibleMatch.getMatchedStudentID(), possibleMatch.getStudentID());
      if (possibleMatchFromStudentOptional.isEmpty()) {
        possibleMatchEntitiesList.add(possibleMatch);
      }
      if (possibleMatchFromMatchedStudentOptional.isEmpty()) {
        possibleMatchEntitiesList.add(PossibleMatchEntity.builder()
            .createDate(possibleMatch.getCreateDate())
            .updateDate(possibleMatch.getUpdateDate())
            .createUser(possibleMatch.getCreateUser())
            .updateUser(possibleMatch.getUpdateUser())
            .matchReasonCode(possibleMatch.getMatchReasonCode())
            .studentID(possibleMatch.getMatchedStudentID())
            .matchedStudentID(possibleMatch.getStudentID())
            .build());
      }
    }
    return possibleMatchEntitiesList;
  }
}
