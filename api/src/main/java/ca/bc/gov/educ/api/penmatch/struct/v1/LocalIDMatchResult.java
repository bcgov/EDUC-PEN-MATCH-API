package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Local id match result.
 */
@Data
@NoArgsConstructor
public class LocalIDMatchResult {

  /**
   * The Id demerits.
   */
  private int idDemerits;
  /**
   * The Local id points.
   */
  private int localIDPoints;

}
