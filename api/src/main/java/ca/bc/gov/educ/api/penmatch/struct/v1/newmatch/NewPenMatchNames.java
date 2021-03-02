package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Pen match names.
 */
@Data
@NoArgsConstructor
public class NewPenMatchNames {
  /**
   * The Legal surname.
   */
  private String legalSurname;
  /**
   * The Legal middle.
   */
  private String legalMiddle;
  /**
   * The Legal given.
   */
  private String legalGiven;
  /**
   * The Usual given.
   */
  private String usualGiven;
  /**
   * The Usual middle.
   */
  private String usualMiddle;
  /**
   * The Usual surname.
   */
  private String usualSurname;
  /**
   * The scrubbed legal surname.
   */
  private String legalSurnameScrubbed;
  /**
   * The scrubbed legal given name.
   */
  private String legalGivenScrubbed;
  /**
   * The scrubbed legal middle name.
   */
  private String legalMiddleScrubbed;
  /**
   * The scrubbed usual surname.
   */
  private String usualSurnameScrubbed;
  /**
   * The scrubbed usual given name.
   */
  private String usualGivenScrubbed;
  /**
   * The legal surname hyphens replaced with blank.
   */
  private String legalSurnameHyphenToBlank;
  /**
   * The legal given hyphens replaced with blank.
   */
  private String legalGivenHyphenToBlank;
  /**
   * The legal middle hyphens replaced with blank.
   */
  private String legalMiddleHyphenToBlank;


}
