package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.NicknameEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The interface Nicknames repository.
 */
@Repository
public interface NicknamesRepository extends CrudRepository<NicknameEntity, String> {
  /**
   * Find all by nickname 1 or nickname 2 list.
   *
   * @param nickname1 the nickname 1
   * @param nickname2 the nickname 2
   * @return the list
   */
  List<NicknameEntity> findAllByNickname1OrNickname2(String nickname1, String nickname2);

  List<NicknameEntity> findAll();

}
