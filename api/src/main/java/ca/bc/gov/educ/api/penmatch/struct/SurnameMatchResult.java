package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SurnameMatchResult {

	private Integer surnamePoints;
	private boolean legalSurnameUsed;

}
