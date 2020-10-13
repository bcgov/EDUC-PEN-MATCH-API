package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Surname match result.
 */
@Data
@NoArgsConstructor
public class SurnameMatchResult {

  /**
   * The Surname points.
   */
  private Integer surnamePoints;
  /**
   * The Legal surname used.
   */
  private boolean legalSurnameUsed;

}
