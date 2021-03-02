package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import ca.bc.gov.educ.api.penmatch.model.v1.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The type New pen match student detail.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NewPenMatchStudentDetail extends PenMatchStudent {

  /**
   * The Pen match transaction names.
   */
//These are updated by the match algorithm
  private NewPenMatchNames penMatchTransactionNames;
  /**
   * The Student true number.
   */
  private String studentTrueNumber;
  /**
   * The Old match f 1 pen.
   */
  private String oldMatchF1PEN;
  /**
   * The Old match f 1 student id.
   */
  private String oldMatchF1StudentID;
  /**
   * The Partial student surname.
   */
  private String partialStudentSurname;
  /**
   * The Partial student given.
   */
  private String partialStudentGiven;
  /**
   * The Min surname search size.
   */
  private Integer minSurnameSearchSize;
  /**
   * The Max surname search size.
   */
  private Integer maxSurnameSearchSize;
  /**
   * The Full surname frequency.
   */
  private Integer fullSurnameFrequency;
  /**
   * The Partial surname frequency.
   */
  private Integer partialSurnameFrequency;
  /**
   * The Given name nicknames.
   */
  private List<NicknamesEntity> givenNameNicknames;
  /**
   * The Middle name nicknames.
   */
  private List<NicknamesEntity> middleNameNicknames;

  /**
   * Instantiates a new New pen match student detail.
   *
   * @param studentDetail       the student detail
   * @param oldMatchF1PEN       the old match f 1 pen
   * @param oldMatchF1StudentID the old match f 1 student id
   */
  @Builder
  public NewPenMatchStudentDetail(PenMatchStudentDetail studentDetail, String oldMatchF1PEN, String oldMatchF1StudentID) {
    this.pen = studentDetail.getPen();
    this.dob = studentDetail.getDob();
    this.sex = studentDetail.getSex();
    this.enrolledGradeCode = studentDetail.getEnrolledGradeCode();
    this.surname = studentDetail.getSurname();
    this.givenName = studentDetail.getGivenName();
    this.middleName = studentDetail.getMiddleName();
    this.usualSurname = studentDetail.getUsualSurname();
    this.usualGivenName = studentDetail.getUsualGivenName();
    this.usualMiddleName = studentDetail.getUsualMiddleName();
    this.mincode = studentDetail.getMincode();
    this.localID = studentDetail.getLocalID();
    this.postal = studentDetail.getPostal();
    this.updateCode = studentDetail.getUpdateCode();
    this.applicationCode = studentDetail.getApplicationCode();
    this.oldMatchF1PEN = oldMatchF1PEN;
    this.oldMatchF1StudentID = oldMatchF1StudentID;
  }


}
