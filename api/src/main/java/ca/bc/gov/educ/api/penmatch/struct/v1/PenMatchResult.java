package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.PriorityQueue;

@Data
@AllArgsConstructor
public class PenMatchResult {

	private PriorityQueue<PenMatchRecord> matchingRecords;
	private String penStatus;
	private String penStatusMessage;
}
