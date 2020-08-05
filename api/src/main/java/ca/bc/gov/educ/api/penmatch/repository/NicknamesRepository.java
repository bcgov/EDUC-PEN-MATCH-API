package ca.bc.gov.educ.api.penmatch.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;

@Repository
public interface NicknamesRepository extends CrudRepository<NicknamesEntity, String> {
	 List<NicknamesEntity> findByNicknames(String nickname1, String nickname2);
  
}
