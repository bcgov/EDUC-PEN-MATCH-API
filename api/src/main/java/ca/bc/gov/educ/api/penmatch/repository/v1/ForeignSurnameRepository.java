package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.ForeignSurnameEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The interface Foreign surname repository.
 */
@Repository
public interface ForeignSurnameRepository extends CrudRepository<ForeignSurnameEntity, String> {
  /**
   * Find by surname and ancestry and effective date less than equal and expiry date greater than equal optional.
   *
   * @param surname       the surname
   * @param ancestry      the ancestry
   * @param effectiveDate the effective date
   * @param expiryDate    the expiry date
   * @return the optional
   */
  Optional<ForeignSurnameEntity> findBySurnameAndAncestryAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(String surname, String ancestry, LocalDate effectiveDate, LocalDate expiryDate);
}
