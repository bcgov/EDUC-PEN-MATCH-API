package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.UUID;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from Jet Stream, which is used in choreography pattern.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  /**
   * The Event repository.
   */
  private final PENMatchEventRepository eventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param eventRepository the student event repository
   */
  @Autowired
  public JetStreamEventHandlerService(final PENMatchEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(final ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      final var eventID = UUID.fromString(choreographedEvent.getEventID());
      final var eventOptional = this.eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        final var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        this.eventRepository.save(studentEvent);
      }
    }
  }
}
