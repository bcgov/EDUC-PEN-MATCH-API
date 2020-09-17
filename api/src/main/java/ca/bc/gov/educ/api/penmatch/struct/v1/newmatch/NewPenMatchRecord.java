package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewPenMatchRecord extends PenMatchRecord {
	private String matchResult;
	private String matchCode;

	@Builder
	public NewPenMatchRecord(String matchResult, String matchCode, String matchingPEN, String studentID){
		super(matchingPEN, studentID);
		this.matchResult = matchResult;
		this.matchCode = matchCode;
	}
}
