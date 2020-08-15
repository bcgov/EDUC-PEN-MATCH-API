package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GivenNameMatchResult {

	private Integer givenNamePoints;
	private boolean givenNameFlip;

}
