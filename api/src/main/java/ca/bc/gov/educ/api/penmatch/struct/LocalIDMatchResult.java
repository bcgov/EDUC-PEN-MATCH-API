package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocalIDMatchResult {

	private Integer idDemerits;
	private Integer localIDPoints;

}
