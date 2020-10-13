package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Middle name match result.
 */
@Data
@NoArgsConstructor
public class MiddleNameMatchResult {

  /**
   * The Middle name points.
   */
  private Integer middleNamePoints;
  /**
   * The Middle name flip.
   */
  private boolean middleNameFlip;

}
