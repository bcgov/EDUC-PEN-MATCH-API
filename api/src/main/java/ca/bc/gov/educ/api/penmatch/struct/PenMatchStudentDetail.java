package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PenMatchStudentDetail extends PenMatchStudent{

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
