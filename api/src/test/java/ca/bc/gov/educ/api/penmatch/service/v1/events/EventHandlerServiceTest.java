package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PossibleMatchMapper;
import ca.bc.gov.educ.api.penmatch.repository.v1.PossibleMatchRepository;
import ca.bc.gov.educ.api.penmatch.service.v1.match.PossibleMatchWrapperService;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The type Event handler service test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventHandlerServiceTest {

  /**
   * The Possible match service.
   */
  @Autowired
  PossibleMatchWrapperService possibleMatchService;


  /**
   * The Event handler service.
   */
  @Autowired
  EventHandlerService eventHandlerService;


  /**
   * The Possible match repository.
   */
  @Autowired
  PossibleMatchRepository possibleMatchRepository;

  /**
   * The Match mapper.
   */
  PossibleMatchMapper matchMapper = PossibleMatchMapper.mapper;


  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    possibleMatchRepository.deleteAll();
  }


  /**
   * Test handle add possible match event given valid payload should store data in db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testHandleAddPossibleMatchEvent_givenValidPayload_shouldStoreDataInDB() throws JsonProcessingException {
    Event event = Event.builder().sagaId(UUID.randomUUID()).eventPayload(JsonUtil.getJsonStringFromObject(getPossibleMatchesPlaceHolderData())).eventType(EventType.ADD_POSSIBLE_MATCH).replyTo("BATCH_API").build();
    var response = eventHandlerService.handleAddPossibleMatchEvent(event);
    assertThat(response.getLeft()).hasSizeGreaterThan(0);
    assertThat(new String(response.getLeft())).contains(EventOutcome.POSSIBLE_MATCH_ADDED.toString());
    assertThat(possibleMatchRepository.findAll()).hasSize(20);
  }

  /**
   * Test handle get possible match event given valid payload should return data from db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testHandleGetPossibleMatchEvent_givenValidPayload_shouldReturnDataFromDB() throws JsonProcessingException {
    var savedMatches = possibleMatchService.savePossibleMatches(getPossibleMatchesPlaceHolderData().stream().map(matchMapper::toModel).collect(Collectors.toList()));
    Event event = Event.builder().sagaId(UUID.randomUUID()).eventPayload(savedMatches.getLeft().get(0).getStudentID().toString()).eventType(EventType.GET_POSSIBLE_MATCH).replyTo("BATCH_API").build();
    var response = eventHandlerService.handleGetPossibleMatchEvent(event);
    assertThat(response).hasSizeGreaterThan(0);
    assertThat(new String(response)).contains(EventOutcome.POSSIBLE_MATCH_FOUND.toString());
    assertThat(possibleMatchRepository.findAll()).hasSize(20);
  }

  /**
   * Test handle delete possible match event given valid payload should delete data from db.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testHandleDeletePossibleMatchEvent_givenValidPayload_shouldDeleteDataFromDB() throws JsonProcessingException {

    List<PossibleMatch> payload = new ArrayList<>();
    var savedMatches = possibleMatchService.savePossibleMatches(getPossibleMatchesPlaceHolderData().stream().map(matchMapper::toModel).collect(Collectors.toList()));
    payload.add(PossibleMatch.builder().studentID(savedMatches.getLeft().get(0).getStudentID())
        .matchedStudentID(savedMatches.getLeft().get(0).getMatchedStudentID())
        .createUser("TEST")
        .updateUser("TEST").build());
    payload.add(PossibleMatch.builder().studentID(savedMatches.getLeft().get(2).getStudentID())
        .matchedStudentID(savedMatches.getLeft().get(2).getMatchedStudentID())
        .createUser("TEST")
        .updateUser("TEST").build());
    Event event = Event.builder().sagaId(UUID.randomUUID()).eventPayload(JsonUtil.getJsonStringFromObject(payload)).eventType(EventType.DELETE_POSSIBLE_MATCH).replyTo("BATCH_API").build();
    var response = eventHandlerService.handleDeletePossibleMatchEvent(event);
    assertThat(response.getLeft()).hasSizeGreaterThan(0);
    assertThat(new String(response.getLeft())).contains(EventOutcome.POSSIBLE_MATCH_DELETED.toString());
    assertThat(possibleMatchRepository.findAll()).hasSize(16);
  }

  /**
   * Gets possible matches place holder data.
   *
   * @return the possible matches place holder data
   */
  private List<PossibleMatch> getPossibleMatchesPlaceHolderData() {
    List<PossibleMatch> possibleMatches = new ArrayList<>();
    var studentID = UUID.randomUUID();
    for (int i = 0; i++ < 10; ) {
      possibleMatches.add(PossibleMatch.builder().studentID(studentID).matchedStudentID(UUID.randomUUID()).matchReasonCode(MatchReasonCodes.PENMATCH).build());
    }
    return possibleMatches;
  }
}
