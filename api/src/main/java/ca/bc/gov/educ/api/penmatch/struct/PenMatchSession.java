package ca.bc.gov.educ.api.penmatch.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenMatchSession {

	private Integer fullSurnameFrequency;
	private Integer partialSurnameFrequency;
	private PenMatchNames penMatchTransactionNames;
	private PenMatchNames penMatchMasterNames;

}
