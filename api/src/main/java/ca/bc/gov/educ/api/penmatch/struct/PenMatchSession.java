package ca.bc.gov.educ.api.penmatch.struct;

import java.util.PriorityQueue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenMatchSession {

	private Integer fullSurnameFrequency;
	private Integer partialSurnameFrequency;
	private PenMatchNames penMatchTransactionNames;
	private Integer minSurnameSearchSize;
	private Integer maxSurnameSearchSize;
	private String alternateLocalID;
	private String partialStudentSurname;
	private String partialStudentGiven;
	private PriorityQueue<PenMatchRecord> matchingRecords;
	private Integer reallyGoodMatches;
	private Integer prettyGoodMatches;
	private String reallyGoodPEN;
	private String studentNumber;
	private String penStatus;
	private String penStatusMessage;
	private String pen1;
	private Integer numberOfMatches;

}
