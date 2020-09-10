package ca.bc.gov.educ.api.penmatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentEntity {
  UUID studentID;
  String pen;
  String legalFirstName;
  String legalMiddleNames;
  String legalLastName;
  String dob;
  String sexCode;
  String genderCode;
  String usualFirstName;
  String usualMiddleNames;
  String usualLastName;
  String email;
  String emailVerified;
  String deceasedDate;
  String postalCode;
  String mincode;
  String localID;
  String gradeCode;
  String gradeYear;
  String demogCode;
  String statusCode;
  String memo;
  String createUser;
  String updateUser;
  String createDate;
  String updateDate;
}
