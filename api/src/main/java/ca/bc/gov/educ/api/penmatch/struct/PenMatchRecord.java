package ca.bc.gov.educ.api.penmatch.struct;

import lombok.*;

/**
 * The type Pen match record.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PenMatchRecord {
  /**
   * The Matching pen.
   */
  private String matchingPEN;
  /**
   * The Student id.
   */
  private String studentID;
}
