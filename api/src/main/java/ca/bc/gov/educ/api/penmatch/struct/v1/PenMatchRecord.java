package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PenMatchRecord {
	private Integer matchingAlgorithmResult;
	private Integer matchingScore;
	private String matchingPEN;
	private String studentID;
}
