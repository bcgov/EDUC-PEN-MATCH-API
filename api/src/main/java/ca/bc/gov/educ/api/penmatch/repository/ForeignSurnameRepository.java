package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.ForeignSurnamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForeignSurnameRepository extends CrudRepository<ForeignSurnamesEntity, String> {
    Optional<ForeignSurnamesEntity> findBySurnameAndAncestryAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(String surname, String ancestry, LocalDate effectiveDate, LocalDate expiryDate);
}
