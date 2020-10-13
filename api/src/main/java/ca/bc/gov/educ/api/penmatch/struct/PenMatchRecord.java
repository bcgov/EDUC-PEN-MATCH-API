package ca.bc.gov.educ.api.penmatch.struct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Pen match record.
 */
@Getter
@Setter
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
