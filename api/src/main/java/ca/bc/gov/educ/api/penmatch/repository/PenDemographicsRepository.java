package ca.bc.gov.educ.api.penmatch.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;

@Repository
public interface PenDemographicsRepository extends CrudRepository<PenDemographicsEntity, String>, PenDemographicsRepositoryCustom {
  Optional<PenDemographicsEntity> findByStudNo(String pen);
  
}
