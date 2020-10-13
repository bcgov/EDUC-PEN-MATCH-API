package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Pen match names.
 */
@Data
@NoArgsConstructor
public class PenMatchNames {
  /**
   * The Legal given.
   */
  private String legalGiven;
  /**
   * The Usual given.
   */
  private String usualGiven;
  /**
   * The Alternate legal given.
   */
  private String alternateLegalGiven;
  /**
   * The Alternate usual given.
   */
  private String alternateUsualGiven;
  /**
   * The Legal middle.
   */
  private String legalMiddle;
  /**
   * The Usual middle.
   */
  private String usualMiddle;
  /**
   * The Alternate legal middle.
   */
  private String alternateLegalMiddle;
  /**
   * The Alternate usual middle.
   */
  private String alternateUsualMiddle;
  /**
   * The Nickname 1.
   */
  private String nickname1;
  /**
   * The Nickname 2.
   */
  private String nickname2;
  /**
   * The Nickname 3.
   */
  private String nickname3;
  /**
   * The Nickname 4.
   */
  private String nickname4;
  /**
   * The Legal surname.
   */
  private String legalSurname;
  /**
   * The Usual surname.
   */
  private String usualSurname;
}
