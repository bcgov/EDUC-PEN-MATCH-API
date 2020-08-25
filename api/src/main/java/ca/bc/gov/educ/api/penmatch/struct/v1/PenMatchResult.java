package ca.bc.gov.educ.api.penmatch.struct.v1;

import java.util.PriorityQueue;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PenMatchResult {

	private PriorityQueue<PenMatchRecord> matchingRecords;
	private String pen;
	private String penStatus;
	private String penStatusMessage;
}
