package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MiddleNameMatchResult {

	private Integer middleNamePoints;
	private boolean middleNameFlip;

}
