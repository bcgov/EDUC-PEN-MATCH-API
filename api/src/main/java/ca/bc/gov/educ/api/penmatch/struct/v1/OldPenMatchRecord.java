package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OldPenMatchRecord extends PenMatchRecord{
	private Integer matchingAlgorithmResult;
	private Integer matchingScore;

	@Builder
	public OldPenMatchRecord(Integer matchingAlgorithmResult, Integer matchingScore, String matchingPEN, String studentID){
		super(matchingPEN, studentID);
		this.matchingAlgorithmResult = matchingAlgorithmResult;
		this.matchingScore = matchingScore;
	}
}
