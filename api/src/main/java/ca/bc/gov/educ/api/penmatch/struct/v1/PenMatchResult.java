package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PenMatchResult {

	private List<PenMatchRecord> matchingRecords;
	private String penStatus;
	private String penStatusMessage;
}
