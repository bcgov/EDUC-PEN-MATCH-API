package ca.bc.gov.educ.api.penmatch.struct;

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
	private PenMatchNames penMatchMasterNames;

	private String penConfirmationResultCode;
	private PenMasterRecord masterRecord;
	private String localStudentNumber;
	private Integer minSurnameSearchSize;
	private Integer maxSurnameSearchSize;
	private String alternateLocalID;
	private Integer totalPoints;
	private String partialStudentSurname;
	private String partialStudentGiven;
	private String[] matchingPENs;
	private Integer[] matchingAlgorithms;
	private Integer[] matchingScores;
	private Integer reallyGoodMatches;
	private Integer prettyGoodMatches;
	private String reallyGoodPEN;
	private String studentNumber;
	private String penStatus;
	private String penStatusMessage;
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

}
