package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MiddleNameMatchResult {

	private Integer middleNamePoints;
	private boolean middleNameFlip;

}
