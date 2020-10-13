package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Given name match result.
 */
@Data
@NoArgsConstructor
public class GivenNameMatchResult {

  /**
   * The Given name points.
   */
  private Integer givenNamePoints;
  /**
   * The Given name flip.
   */
  private boolean givenNameFlip;

}
