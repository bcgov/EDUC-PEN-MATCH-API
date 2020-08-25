package ca.bc.gov.educ.api.penmatch.schedulers;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.messaging.MessagePublisher;
import ca.bc.gov.educ.api.penmatch.model.PENMatchEvent;
import ca.bc.gov.educ.api.penmatch.repository.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.Event;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.penmatch.constants.Topics.PEN_MATCH_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EventTaskScheduler {

  @Getter(PRIVATE)
  private final MessagePublisher messagePubSub;
  @Getter(PRIVATE)
  private final PENMatchEventRepository penMatchEventRepository;

  @Autowired
  public EventTaskScheduler(MessagePublisher messagePubSub, PENMatchEventRepository penMatchEventRepository) {
    this.messagePubSub = messagePubSub;
    this.penMatchEventRepository = penMatchEventRepository;
  }

  @Scheduled(cron = "0/5 * * * * *")
  @SchedulerLock(name = "EventTablePoller",
          lockAtLeastFor = "4s", lockAtMostFor = "4s")
  public void pollEventTableAndPublish() throws InterruptedException, IOException, TimeoutException {
    List<PENMatchEvent> events = getPenMatchEventRepository().findByEventStatus(DB_COMMITTED.toString());
    if (!events.isEmpty()) {
      for (var event : events) {
        try {
          if (Optional.ofNullable(event.getReplyChannel()).isPresent()) {
            getMessagePubSub().dispatchMessage(event.getReplyChannel(), penMatchEventProcessed(event));
          }
          getMessagePubSub().dispatchMessage(PEN_MATCH_API_TOPIC.toString(), createOutboxEvent(event));
        } catch (InterruptedException | TimeoutException | IOException e) {
          log.error("exception occurred", e);
          throw e;
        }
      }
    } else {
      log.trace("no unprocessed records.");
    }
  }

  private byte[] penMatchEventProcessed(PENMatchEvent penMatchEvent) throws JsonProcessingException {
    Event event = Event.builder()
            .sagaId(penMatchEvent.getSagaId())
            .eventType(EventType.valueOf(penMatchEvent.getEventType()))
            .eventOutcome(EventOutcome.valueOf(penMatchEvent.getEventOutcome()))
            .eventPayload(penMatchEvent.getEventPayload()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }

  private byte[] createOutboxEvent(PENMatchEvent penMatchEvent) throws JsonProcessingException {
    Event event = Event.builder().eventType(EventType.PEN_MATCH_EVENT_OUTBOX_PROCESSED).eventPayload(penMatchEvent.getEventId().toString()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }
}
