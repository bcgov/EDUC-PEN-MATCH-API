package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.PriorityQueue;

@Data
@NoArgsConstructor
public class NewPenMatchSession {

    private PriorityQueue<NewPenMatchRecord> matchingRecords;
    private Integer reallyGoodMatches;
    private Integer prettyGoodMatches;
    private String reallyGoodPEN;
    private String studentNumber;
    private String penStatus;
    private String penStatusMessage;
    private String pen1;
    private Integer numberOfMatches;
    private boolean isPSI;
}
