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
    private String penStatus;
    private String penStatusMessage;
    private String pen1;
    private Integer numberOfMatches;
    private String applicationCode;
    private String bestMatchPEN;
    private String bestMatchCode;
    private String bestMatchValue;
    private boolean assignNewPEN;
    private boolean isPSI;
}
