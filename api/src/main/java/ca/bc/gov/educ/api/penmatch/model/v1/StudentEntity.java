package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * The type Student entity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentEntity {
  /**
   * The Student id.
   */
  UUID studentID;
  /**
   * The Pen.
   */
  String pen;
  /**
   * The Legal first name.
   */
  String legalFirstName;
  /**
   * The Legal middle names.
   */
  String legalMiddleNames;
  /**
   * The Legal last name.
   */
  String legalLastName;
  /**
   * The Dob.
   */
  String dob;
  /**
   * The Sex code.
   */
  String sexCode;
  /**
   * The Gender code.
   */
  String genderCode;
  /**
   * The Usual first name.
   */
  String usualFirstName;
  /**
   * The Usual middle names.
   */
  String usualMiddleNames;
  /**
   * The Usual last name.
   */
  String usualLastName;
  /**
   * The Email.
   */
  String email;
  /**
   * The Email verified.
   */
  String emailVerified;
  /**
   * The Deceased date.
   */
  String deceasedDate;
  /**
   * The Postal code.
   */
  String postalCode;
  /**
   * The Mincode.
   */
  String mincode;
  /**
   * The Local id.
   */
  String localID;
  /**
   * The Grade code.
   */
  String gradeCode;
  /**
   * The Grade year.
   */
  String gradeYear;
  /**
   * The Demog code.
   */
  String demogCode;
  /**
   * The Status code.
   */
  String statusCode;
  /**
   * The Memo.
   */
  String memo;
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
  /**
   * The Create date.
   */
  String createDate;
  /**
   * The Update date.
   */
  String updateDate;

  String trueStudentID;
}
