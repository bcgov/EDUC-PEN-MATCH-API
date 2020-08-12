package ca.bc.gov.educ.api.penmatch.struct;

import ca.bc.gov.educ.api.penmatch.enumeration.PenAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckForMatchResult {

	private boolean matchFound;
	private boolean type5Match;
	private boolean type5F1;
	private PenAlgorithm algorithmUsed;
	
}
