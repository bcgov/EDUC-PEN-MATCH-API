package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.PriorityQueue;

@Data
@AllArgsConstructor
public class NewPenMatchResult {

	private PriorityQueue<NewPenMatchRecord> matchingRecords;
	private String pen;
	private String penStatus;
	private String penStatusMessage;
}
