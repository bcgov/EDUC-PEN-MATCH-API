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
	private String pen;
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
	private String updateCode;
	
	//These are updated by the match algorithm
	private PenMatchNames penMatchTransactionNames;
	private String alternateLocalID;
	private Integer minSurnameSearchSize;
	private Integer maxSurnameSearchSize;
	private String partialStudentSurname;
	private String partialStudentGiven;
	private Integer fullSurnameFrequency;
	private Integer partialSurnameFrequency;
}
