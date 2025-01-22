package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.MatchCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The interface Match codes repository.
 */
@Repository
public interface MatchCodesRepository extends CrudRepository<MatchCodeEntity, String> {
  /**
   * Find by match code optional.
   *
   * @param matchCode the match code
   * @return the optional
   */
  Optional<MatchCodeEntity> findByMatchCode(String matchCode);

  List<MatchCodeEntity> findAll();
}
