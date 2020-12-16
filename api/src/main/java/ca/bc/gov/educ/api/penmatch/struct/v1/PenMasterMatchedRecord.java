package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Pen master match record.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PenMasterMatchedRecord {
  /**
   * The master record
   */
  private PenMasterRecord masterRecord;

  /**
   * The total score.
   */
  private int totalScore;
}
