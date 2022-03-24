package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.PENMatchEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

  @Transactional
  @Modifying
  @Query("delete from PENMatchEvent where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
