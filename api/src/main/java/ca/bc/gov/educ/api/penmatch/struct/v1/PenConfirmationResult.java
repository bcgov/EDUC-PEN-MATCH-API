package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Pen confirmation result.
 */
@Data
@NoArgsConstructor
public class PenConfirmationResult {
  /**
   * The constant PEN_CONFIRMED.
   */
  public static final String PEN_CONFIRMED = "PEN_CONFIRMED";
  /**
   * The constant PEN_ON_FILE.
   */
  public static final String PEN_ON_FILE = "PEN_ON_FILE";
  /**
   * The constant NO_RESULT.
   */
  public static final String NO_RESULT = "NO_RESULT";

  /**
   * The Merged pen.
   */
  private String mergedPEN;
  /**
   * The Deceased.
   */
  private boolean deceased;
  /**
   * The Pen confirmation result code.
   */
  private String penConfirmationResultCode;
  /**
   * The Master record.
   */
  private PenMasterRecord masterRecord;
}
