package ca.bc.gov.educ.api.penmatch.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;

@Repository
public interface SurnameFrequencyRepository extends CrudRepository<SurnameFrequencyEntity, String> {
	Optional<SurnameFrequencyEntity> findBySurname(String surname);
}
