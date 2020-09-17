package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.PriorityQueue;

@Data
@NoArgsConstructor
public class PenMatchSession {
	private PriorityQueue<OldPenMatchRecord> matchingRecords;
	private PenMasterRecord prettyGoodMatchRecord;
	private PenMasterRecord reallyGoodMasterRecord;
	private String penStatus;
	private String penStatusMessage;
}
