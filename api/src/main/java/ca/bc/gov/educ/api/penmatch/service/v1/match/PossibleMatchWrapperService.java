package ca.bc.gov.educ.api.penmatch.service.v1.match;

import ca.bc.gov.educ.api.penmatch.model.v1.MatchReasonCodeEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.model.v1.PossibleMatchEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is just a wrapper over {@link PossibleMatchService} to make transactions boundaries atomic for the controller.
 */
@Slf4j
@Service
public class PossibleMatchWrapperService {
  /**
   * The Possible match service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final PossibleMatchService possibleMatchService;

  /**
   * Instantiates a new Possible match wrapper service.
   *
   * @param possibleMatchService the possible match service
   */
  @Autowired
  public PossibleMatchWrapperService(PossibleMatchService possibleMatchService) {
    this.possibleMatchService = possibleMatchService;
  }

  /**
   * Save possible matches pair.
   *
   * @param possibleMatchEntities the possible match entities
   * @return the pair
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Pair<List<PossibleMatchEntity>, Optional<PENMatchEvent>> savePossibleMatches(List<PossibleMatchEntity> possibleMatchEntities) throws JsonProcessingException {
    return getPossibleMatchService().savePossibleMatches(possibleMatchEntities);
  }

  /**
   * Gets possible matches.
   *
   * @param studentID the student id
   * @return the possible matches
   */
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public List<PossibleMatchEntity> getPossibleMatches(UUID studentID) {
    return getPossibleMatchService().getPossibleMatches(studentID);
  }

  /**
   * Delete possible matches optional.
   *
   * @param possibleMatches the possible matches
   * @return the optional
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Optional<PENMatchEvent> deletePossibleMatches(List<PossibleMatch> possibleMatches) throws JsonProcessingException {
    return getPossibleMatchService().deletePossibleMatches(possibleMatches);
  }

  /**
   * Gets all match reason codes.
   *
   * @return the all match reason codes
   */
  public List<MatchReasonCodeEntity> getAllMatchReasonCodes() {
    return getPossibleMatchService().getAllMatchReasonCodes();
  }
}
