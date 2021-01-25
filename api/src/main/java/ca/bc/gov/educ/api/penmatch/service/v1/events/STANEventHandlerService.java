package ca.bc.gov.educ.api.penmatch.service.v1.events;

import ca.bc.gov.educ.api.penmatch.repository.v1.PENMatchEventRepository;
import ca.bc.gov.educ.api.penmatch.struct.ChoreographedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

import static ca.bc.gov.educ.api.penmatch.constants.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from STAN, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class STANEventHandlerService {

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
  public STANEventHandlerService(PENMatchEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        eventRepository.save(studentEvent);
      }
    }
  }
}
