package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Pen match names.
 */
@Data
public class PenMatchNames {

  public PenMatchNames() {
    this.nicknames = new ArrayList<>();
  }

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
   * The Nicknames.
   */
  private List<String> nicknames;
  /**
   * The Legal surname.
   */
  private String legalSurname;
  /**
   * The Usual surname.
   */
  private String usualSurname;
}
