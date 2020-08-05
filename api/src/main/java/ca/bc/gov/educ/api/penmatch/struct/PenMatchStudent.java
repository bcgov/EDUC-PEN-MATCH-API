package ca.bc.gov.educ.api.penmatch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenMatchStudent {
  private String studentNumber;
  private String penStatus;
  private String penStatusMessage;
  private String dob;
  private String sex;
  private String enrolledGradeCode;
  private String surname;
  private String givenName;
  private String givenInitial;
  private String middleName;
  private String middleInitial;
  private String usualSurname;
  private String usualGivenName;
  private String usualGivenInitial;
  private String usualMiddleName;
  private String usualMiddleInitial;
  private String mincode;
  private String localID;
  private String postal;
  private String fypFlag;
  private String assignmentCode;
  private String assignmentDate;
  private String updateCode;
  private String version;
  private String pen1;
  private String pen2;
  private String pen3;
  private String pen4;
  private String pen5;
  private String pen6;
  private String pen7;
  private String pen8;
  private String pen9;
  private String pen10;
  private String pen11;
  private String pen12;
  private String pen13;
  private String pen14;
  private String pen15;
  private String pen16;
  private String pen17;
  private String pen18;
  private String pen19;
  private String pen20;
  private Integer numberOfMatches;
  private boolean deceased;
}


