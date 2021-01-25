package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static ca.bc.gov.educ.api.penmatch.constants.EventOutcome.POSSIBLE_MATCH_ADDED;
import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.penmatch.constants.EventType.ADD_POSSIBLE_MATCH;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class STANEventHandlerServiceTest {

  @Autowired
  STANEventHandlerService stanEventHandlerService;

  @Autowired
  PENMatchEventRepository penMatchEventRepository;

  @After
  public void tearDown() {
    penMatchEventRepository.deleteAll();
  }

  @Test
  public void testUpdateEventStatus_givenNoDataInDB_shouldDONothing() throws JsonProcessingException {
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventID(UUID.randomUUID().toString());
    choreographedEvent.setEventOutcome(POSSIBLE_MATCH_ADDED);
    choreographedEvent.setEventType(ADD_POSSIBLE_MATCH);
    choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(new ArrayList<>()));
    stanEventHandlerService.updateEventStatus(choreographedEvent);
    var results = penMatchEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).isEmpty();
  }

  @Test
  public void testUpdateEventStatus_givenEventIdNull_shouldDONothing() throws JsonProcessingException {
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventOutcome(POSSIBLE_MATCH_ADDED);
    choreographedEvent.setEventType(ADD_POSSIBLE_MATCH);
    choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(new ArrayList<>()));
    stanEventHandlerService.updateEventStatus(choreographedEvent);
    var results = penMatchEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).isEmpty();
  }

  @Test
  public void testUpdateEventStatus_givenChoreographedEventNull_shouldDONothing() {
    stanEventHandlerService.updateEventStatus(null);
    var results = penMatchEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).isEmpty();
  }

  @Test
  public void testUpdateEventStatus_givenDataInDB_shouldUpdateStatus() throws JsonProcessingException {
    var studentEvent = penMatchEventRepository.save(createStudentEvent());
    ChoreographedEvent choreographedEvent = new ChoreographedEvent();
    choreographedEvent.setEventID(studentEvent.getEventId().toString());
    choreographedEvent.setEventOutcome(POSSIBLE_MATCH_ADDED);
    choreographedEvent.setEventType(ADD_POSSIBLE_MATCH);
    choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(new ArrayList<>()));
    stanEventHandlerService.updateEventStatus(choreographedEvent);
    var results = penMatchEventRepository.findByEventStatus(MESSAGE_PUBLISHED.toString());
    assertThat(results).hasSize(1);
    assertThat(results.get(0)).isNotNull();
  }

  private PENMatchEvent createStudentEvent() throws JsonProcessingException {
    return PENMatchEvent.builder()
        .eventId(UUID.randomUUID())
        .createDate(LocalDateTime.now())
        .createUser("TEST")
        .eventOutcome(POSSIBLE_MATCH_ADDED.toString())
        .eventStatus(DB_COMMITTED.toString())
        .eventType(ADD_POSSIBLE_MATCH.toString())
        .eventPayload(JsonUtil.getJsonStringFromObject(new ArrayList<>()))
        .updateDate(LocalDateTime.now())
        .updateUser("TEST")
        .build();
  }
}
