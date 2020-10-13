package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.constants.PenAlgorithm;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Check for match result.
 */
@Data
@NoArgsConstructor
public class CheckForMatchResult {

  /**
   * The Match found.
   */
  private boolean matchFound;
  /**
   * The Type 5 match.
   */
  private boolean type5Match;
  /**
   * The Type 5 f 1.
   */
  private boolean type5F1;
  /**
   * The Total points.
   */
  private Integer totalPoints;
  /**
   * The Algorithm used.
   */
  private PenAlgorithm algorithmUsed;
  /**
   * The Really good matches.
   */
  private Integer reallyGoodMatches;
  /**
   * The Pretty good matches.
   */
  private Integer prettyGoodMatches;
  /**
   * The Really good pen.
   */
  private String reallyGoodPEN;
}
