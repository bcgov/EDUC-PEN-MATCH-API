package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchSession;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.PriorityQueue;

@Data
@NoArgsConstructor
public class NewPenMatchSession extends PenMatchSession {

    private boolean isPSI;
}
