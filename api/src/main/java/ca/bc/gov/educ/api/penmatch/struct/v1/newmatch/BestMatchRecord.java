package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Best match record.
 */
@Getter
@Setter
@AllArgsConstructor
public class BestMatchRecord {
  /**
   * The Match value.
   */
  private Long matchValue;
  /**
   * The Match code.
   */
  private String matchCode;
  /**
   * The Match pen.
   */
  private String matchPEN;
  /**
   * The Student id.
   */
  private String studentID;
}
