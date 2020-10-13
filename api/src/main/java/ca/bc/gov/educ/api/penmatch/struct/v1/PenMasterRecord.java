package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Pen master record.
 */
@Data
@NoArgsConstructor
public class PenMasterRecord {
  /**
   * The Student id.
   */
  private String studentID;
  /**
   * The Archive flag.
   */
  private String archiveFlag;
  /**
   * The Pen.
   */
  private String pen;
  /**
   * The Surname.
   */
  private String surname;
  /**
   * The Given.
   */
  private String given;
  /**
   * The Middle.
   */
  private String middle;
  /**
   * The Usual surname.
   */
  private String usualSurname;
  /**
   * The Usual given name.
   */
  private String usualGivenName;
  /**
   * The Usual middle name.
   */
  private String usualMiddleName;
  /**
   * The Province code.
   */
  private String provinceCode;
  /**
   * The Country code.
   */
  private String countryCode;
  /**
   * The Postal.
   */
  private String postal;
  /**
   * The Dob.
   */
  private String dob;
  /**
   * The Sex.
   */
  private String sex;
  /**
   * The Grade.
   */
  private String grade;
  /**
   * The Citizenship.
   */
  private String citizenship;
  /**
   * The Status.
   */
  private String status;
  /**
   * The Home language.
   */
  private String homeLanguage;
  /**
   * The Aboriginal indicator.
   */
  private String aboriginalIndicator;
  /**
   * The Band code.
   */
  private String bandCode;
  /**
   * The Merged from pen.
   */
  private String mergedFromPEN;
  /**
   * The Mincode.
   */
  private String mincode;
  /**
   * The Local id.
   */
  private String localId;

  /**
   * The Alternate local id.
   */
  private String alternateLocalId;
}
