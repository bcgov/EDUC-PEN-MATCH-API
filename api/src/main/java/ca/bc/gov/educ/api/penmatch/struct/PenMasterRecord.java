package ca.bc.gov.educ.api.penmatch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenMasterRecord {
    private String  masterArchiveFlag;
    private String  masterStudentNumber;
    private String  masterStudentSurname;
    private String  masterStudentGiven;
    private String  masterStudentMiddle;
    private String  masterUsualSurname;
    private String  masterUsualGivenName;
    private String  masterUsualMiddleName;
    private String  masterProvinceCode;
    private String  masterCountryCode;
    private String  masterPostal;
    private String  masterStudentDob;
    private String  masterStudentSex;
    private String  masterStudentGrade; 
    private String  masterStudentCitizenship;
    private String  masterStudentTrueNumber;
    private String  masterStudentStatus;
    private String  masterHomeLanguage;        
    private String  masterAboriginalIndicator;   
    private String  masterBandCode;       
    private String  masterMergedFromPEN;  
    private String  masterPenMincode;      
    private String  masterPenLocalId;     
}
