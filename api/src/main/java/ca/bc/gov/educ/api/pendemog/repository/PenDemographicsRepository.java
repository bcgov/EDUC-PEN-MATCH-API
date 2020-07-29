package ca.bc.gov.educ.api.pendemog.repository;

import ca.bc.gov.educ.api.pendemog.model.PenDemographicsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PenDemographicsRepository extends CrudRepository<PenDemographicsEntity, String>, PenDemographicsRepositoryCustom {
  Optional<PenDemographicsEntity> findByStudNo(String pen);
}
