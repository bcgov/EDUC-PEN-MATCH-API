package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PenMatchNames {
	private String legalGiven;
	private String usualGiven;
	private String alternateLegalGiven;
	private String alternateUsualGiven;
	private String legalMiddle;
	private String usualMiddle;
	private String alternateLegalMiddle;
	private String alternateUsualMiddle;
	private String nickname1;
	private String nickname2;
	private String nickname3;
	private String nickname4;
}
