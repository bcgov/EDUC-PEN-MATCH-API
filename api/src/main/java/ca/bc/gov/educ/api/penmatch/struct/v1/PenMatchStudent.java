package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * The type Pen match student.
 */
@Data
@NoArgsConstructor
public class PenMatchStudent {
  /**
   * The Pen.
   */
  protected String pen;
  /**
   * The Dob.
   */
  @Pattern(regexp = "^((19|20)\\d\\d)(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$")
  @NotNull(message = "Date of Birth can not be null.")
  protected String dob;
  /**
   * The Sex.
   */
  @NotNull(message = "Sex Code can not be null.")
  protected String sex;
  /**
   * The Enrolled grade code.
   */
  protected String enrolledGradeCode;
  /**
   * The Surname.
   */
  @Size(min = 2, max = 255)
  @NotNull(message = "Surname can not be null.")
  protected String surname;
  /**
   * The Given name.
   */
  @Size(min = 2, max = 255)
  @NotNull(message = "Given name can not be null.")
  protected String givenName;
  /**
   * The Middle name.
   */
  protected String middleName;
  /**
   * The Usual surname.
   */
  protected String usualSurname;
  /**
   * The Usual given name.
   */
  protected String usualGivenName;
  /**
   * The Usual middle name.
   */
  protected String usualMiddleName;
  /**
   * The Mincode.
   */
  protected String mincode;
  /**
   * The Local id.
   */
  protected String localID;
  /**
   * The Postal.
   */
  protected String postal;
  /**
   * The Update code.
   */
  protected String updateCode;
  /**
   * The Application code.
   */
  protected String applicationCode;
  /**
   * The Assign new pen.
   */
  protected boolean assignNewPEN;

}
