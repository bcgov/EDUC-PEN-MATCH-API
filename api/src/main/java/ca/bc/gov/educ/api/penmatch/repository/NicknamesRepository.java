package ca.bc.gov.educ.api.penmatch.repository;

import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NicknamesRepository extends CrudRepository<NicknamesEntity, String> {
	List<NicknamesEntity> findAllByNickname1OrNickname2(String nickname1, String nickname2);

}
