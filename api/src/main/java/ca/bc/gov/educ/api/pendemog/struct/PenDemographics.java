package ca.bc.gov.educ.api.pendemog.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenDemographics {
  private String pen;
  private String studSurname;
  private String studGiven;
  private String studMiddle;
  private String usualSurname;
  private String usualGiven;
  private String usualMiddle;
  private String studBirth;
  private String studSex;
  private String studStatus;
  private String localID;
  private String postalCode;
  private String grade;
  private String gradeYear;
  private String demogCode;
  private String mincode;
  private String createDate;
  private String createUserName;
}
