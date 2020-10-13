package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.ForeignSurnamesEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The interface Foreign surname repository.
 */
@Repository
public interface ForeignSurnameRepository extends CrudRepository<ForeignSurnamesEntity, String> {
  /**
   * Find by surname and ancestry and effective date less than equal and expiry date greater than equal optional.
   *
   * @param surname       the surname
   * @param ancestry      the ancestry
   * @param effectiveDate the effective date
   * @param expiryDate    the expiry date
   * @return the optional
   */
  Optional<ForeignSurnamesEntity> findBySurnameAndAncestryAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(String surname, String ancestry, LocalDate effectiveDate, LocalDate expiryDate);
}
