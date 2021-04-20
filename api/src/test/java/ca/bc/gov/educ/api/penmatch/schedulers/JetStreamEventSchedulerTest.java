package ca.bc.gov.educ.api.penmatch.schedulers;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventStatus;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import ca.bc.gov.educ.api.penmatch.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class JetStreamEventSchedulerTest {

  @Autowired
  PENMatchEventRepository eventRepository;

  @Autowired
  JetStreamEventScheduler jetStreamEventScheduler;

  @Autowired
  NatsConnection natsConnection;

  @Autowired
  Publisher publisher;

  @Before
  public void before() {
    LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @After
  public void tearDown() {
    this.eventRepository.deleteAll();
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenNoRecordsInDB_shouldDoNothing() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.jetStreamEventScheduler.findAndPublishStudentEventsToJetStream();
    verify(this.publisher, atMost(invocations)).dispatchChoreographyEvent(any());
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenRecordsInDBButLessThan5Minutes_shouldDoNothing() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.eventRepository.save(this.createPlaceHolderEvent(1));
    this.jetStreamEventScheduler.findAndPublishStudentEventsToJetStream();
    verify(this.publisher, atMost(invocations)).dispatchChoreographyEvent(any());
  }

  @Test
  public void testFindAndPublishStudentEventsToSTAN_givenRecordsInDBButGreaterThan5Minutes_shouldSendMessagesToSTAN() {
    final var invocations = mockingDetails(this.publisher).getInvocations().size();
    this.eventRepository.save(this.createPlaceHolderEvent(10));
    this.jetStreamEventScheduler.findAndPublishStudentEventsToJetStream();
    verify(this.publisher, atLeast(invocations + 1)).dispatchChoreographyEvent(any());
  }

  private PENMatchEvent createPlaceHolderEvent(final int subtractMinutes) {
    return PENMatchEvent.builder().eventPayload("test_payload").eventId(UUID.randomUUID()).eventType(EventType.DELETE_POSSIBLE_MATCH.toString())
        .eventOutcome(EventOutcome.POSSIBLE_MATCH_DELETED.toString())
        .createDate(LocalDateTime.now().minusMinutes(subtractMinutes))
        .updateDate(LocalDateTime.now().minusMinutes(subtractMinutes))
        .eventStatus(EventStatus.DB_COMMITTED.toString())
        .createUser("TEST")
        .updateUser("TEST")
        .build();
  }
}
