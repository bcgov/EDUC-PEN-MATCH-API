package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.MatchCodesEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchCodesRepository extends CrudRepository<MatchCodesEntity, String> {
	Optional<MatchCodesEntity> findByMatchCode(String matchCode);

	List<MatchCodesEntity> findAll();
}
