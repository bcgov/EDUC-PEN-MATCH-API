package ca.bc.gov.educ.api.penmatch.service.v1.match;

import ca.bc.gov.educ.api.penmatch.model.v1.MatchReasonCodeEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.PossibleMatchEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchReasonCodeRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.PossibleMatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
   * Instantiates a new Possible match service.
   *
   * @param possibleMatchRepository   the possible match repository
   * @param matchReasonCodeRepository the match reason code repository
   */
  @Autowired
  public PossibleMatchService(PossibleMatchRepository possibleMatchRepository, MatchReasonCodeRepository matchReasonCodeRepository) {
    this.possibleMatchRepository = possibleMatchRepository;
    this.matchReasonCodeRepository = matchReasonCodeRepository;
  }

  /**
   * Save possible matches list.
   *
   * @param possibleMatchEntities the possible match entities
   * @return the list
   */
  @Transactional
  public List<PossibleMatchEntity> savePossibleMatches(List<PossibleMatchEntity> possibleMatchEntities) {
    var updatedList = populateTwoWayAssociationOfEntities(possibleMatchEntities);
    if (!updatedList.isEmpty()) {
      return possibleMatchRepository.saveAll(updatedList);
    }
    return new ArrayList<>();
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
   * @param studentID        the student id
   * @param matchedStudentID the matched student id
   */
  @Transactional
  public void deletePossibleMatches(UUID studentID, UUID matchedStudentID) {
    var possibleMatchesFound = new ArrayList<PossibleMatchEntity>();
    var possibleMatchFromStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(studentID, matchedStudentID);
    var possibleMatchFromMatchedStudentOptional = possibleMatchRepository.findByStudentIDAndMatchedStudentID(matchedStudentID, studentID);
    possibleMatchFromStudentOptional.ifPresent(possibleMatchesFound::add);
    possibleMatchFromMatchedStudentOptional.ifPresent(possibleMatchesFound::add);
    if (!possibleMatchesFound.isEmpty()) {
      possibleMatchRepository.deleteAll(possibleMatchesFound);
    }
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
