package ca.bc.gov.educ.api.penmatch.schedulers;

import ca.bc.gov.educ.api.penmatch.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.DB_COMMITTED;


/**
 * This class is responsible to check the PEN_MATCH_EVENT table periodically and publish messages to Jet Stream, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

  /**
   * The Event repository.
   */
  private final PENMatchEventRepository eventRepository;
  /**
   * The Publisher.
   */
  private final Publisher publisher;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param eventRepository the event repository
   * @param publisher       the publisher
   */
  public JetStreamEventScheduler(final PENMatchEventRepository eventRepository, final Publisher publisher) {
    this.eventRepository = eventRepository;
    this.publisher = publisher;
  }

  /**
   * Find and publish student events to stan.
   */
  @Scheduled(cron = "${cron.scheduled.publish.events.stan}") // every 5 minutes
  @SchedulerLock(name = "PUBLISH_PEN_MATCH_EVENTS_TO_JET_STREAM", lockAtLeastFor = "${cron.scheduled.publish.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.publish.events.stan" +
      ".lockAtMostFor}")
  public void findAndPublishStudentEventsToJetStream() {
    LockAssert.assertLocked();
    final var results = this.eventRepository.findByEventStatus(DB_COMMITTED.toString());
    if (!results.isEmpty()) {
      results.stream()
          .filter(el -> el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5)))
          .collect(Collectors.toList())
          .forEach(this.publisher::dispatchChoreographyEvent);
    }
  }
}
