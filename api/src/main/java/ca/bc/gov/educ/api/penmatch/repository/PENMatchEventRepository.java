package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.PENMatchEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Pen match event repository.
 */
@Repository
public interface PENMatchEventRepository extends CrudRepository<PENMatchEvent, UUID> {
  /**
   * Find by event status list.
   *
   * @param status the status
   * @return the list
   */
  List<PENMatchEvent> findByEventStatus(String status);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId   the saga id
   * @param toString the to string
   * @return the optional
   */
  Optional<PENMatchEvent> findBySagaIdAndEventType(UUID sagaId, String toString);
}
