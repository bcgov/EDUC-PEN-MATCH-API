package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Surname frequency repository.
 */
@Repository
public interface SurnameFrequencyRepository extends CrudRepository<SurnameFrequencyEntity, String> {
  /**
   * Find all by surname starting with list.
   *
   * @param surname the surname
   * @return the list
   */
  List<SurnameFrequencyEntity> findAllBySurnameStartingWith(String surname);
}
