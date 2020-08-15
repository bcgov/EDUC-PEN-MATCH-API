package ca.bc.gov.educ.api.penmatch.struct;

import ca.bc.gov.educ.api.penmatch.enumeration.PenAlgorithm;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckForMatchResult {

	private boolean matchFound;
	private boolean type5Match;
	private boolean type5F1;
	private Integer totalPoints;
	private PenAlgorithm algorithmUsed;
	private Integer reallyGoodMatches;
	private Integer prettyGoodMatches;
	private String reallyGoodPEN;
}
