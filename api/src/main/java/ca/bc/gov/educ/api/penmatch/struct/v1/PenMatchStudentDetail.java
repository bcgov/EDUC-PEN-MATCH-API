package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
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
