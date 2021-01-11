package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.PossibleMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Possible match repository.
 */
@Repository
public interface PossibleMatchRepository extends JpaRepository<PossibleMatchEntity, UUID> {
  /**
   * Find all by student id list.
   *
   * @param studentID the student id
   * @return the list
   */
  List<PossibleMatchEntity> findAllByStudentID(UUID studentID);

  /**
   * Find by student id and matched student id optional.
   *
   * @param studentID        the student id
   * @param matchedStudentID the matched student id
   * @return the optional
   */
  Optional<PossibleMatchEntity> findByStudentIDAndMatchedStudentID(UUID studentID, UUID matchedStudentID);
}
