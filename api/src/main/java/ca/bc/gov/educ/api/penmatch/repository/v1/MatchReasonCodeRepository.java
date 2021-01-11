package ca.bc.gov.educ.api.penmatch.repository.v1;

import ca.bc.gov.educ.api.penmatch.model.v1.MatchReasonCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The interface Match reason code repository.
 */
@Repository
public interface MatchReasonCodeRepository extends JpaRepository<MatchReasonCodeEntity, String> {
}
