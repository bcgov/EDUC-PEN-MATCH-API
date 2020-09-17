package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewPenMatchNameChangeResult {
	private String matchResult;
	private String matchCode;
}
