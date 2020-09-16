package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurnameFrequencyRepository extends CrudRepository<SurnameFrequencyEntity, String> {
	List<SurnameFrequencyEntity> findAllBySurnameStartingWith(String surname);
}
