package ca.bc.gov.educ.api.penmatch.struct;

import java.util.PriorityQueue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenMatchResult {

	private PriorityQueue<PenMatchRecord> matchingRecords;
	private String pen;
	private String penStatus;
	private String penStatusMessage;
}
