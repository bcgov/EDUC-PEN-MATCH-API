package ca.bc.gov.educ.api.penmatch.compare;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchRecord;

import java.util.Comparator;

public class NewPenMatchComparator implements Comparator<NewPenMatchRecord> {
    @Override
    public int compare(NewPenMatchRecord x, NewPenMatchRecord y) {
        return 0;
    }
}
