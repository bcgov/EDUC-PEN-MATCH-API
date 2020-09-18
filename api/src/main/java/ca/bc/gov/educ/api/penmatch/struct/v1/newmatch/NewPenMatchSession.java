package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.PriorityQueue;

@Data
@NoArgsConstructor
public class NewPenMatchSession {

    private List<NewPenMatchRecord> matchingRecordsList;
    private PriorityQueue<BestMatchRecord> matchingRecordsQueue;
    private String penStatus;
    private String penStatusMessage;
    private boolean isPSI;
}
