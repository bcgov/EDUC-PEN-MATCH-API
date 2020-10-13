package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * this is used as a placeholder for computational purpose in the app.
 * this is created to create a separation between input to the api and the object which api will process and populate.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PenMatchStudentDetail extends PenMatchStudent {

  /**
   * The Pen match transaction names.
   */
//These are updated by the match algorithm
  private PenMatchNames penMatchTransactionNames;
  /**
   * The Alternate local id.
   */
  private String alternateLocalID;
  /**
   * The Min surname search size.
   */
  private Integer minSurnameSearchSize;
  /**
   * The Max surname search size.
   */
  private Integer maxSurnameSearchSize;
  /**
   * The Partial student surname.
   */
  private String partialStudentSurname;
  /**
   * The Partial student given.
   */
  private String partialStudentGiven;
  /**
   * The Full surname frequency.
   */
  private Integer fullSurnameFrequency;
  /**
   * The Partial surname frequency.
   */
  private Integer partialSurnameFrequency;
}
