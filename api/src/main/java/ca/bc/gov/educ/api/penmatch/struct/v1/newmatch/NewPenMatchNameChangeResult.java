package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The type New pen match name change result.
 */
@Getter
@Setter
@AllArgsConstructor
public class NewPenMatchNameChangeResult {
  /**
   * The Match result.
   */
  private String matchResult;
  /**
   * The Match code.
   */
  private String matchCode;
}
