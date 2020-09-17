package ca.bc.gov.educ.api.penmatch.compare;

import ca.bc.gov.educ.api.penmatch.struct.v1.OldPenMatchRecord;

import java.util.Comparator;

public class PenMatchComparator implements Comparator<OldPenMatchRecord> {
    @Override
    public int compare(OldPenMatchRecord x, OldPenMatchRecord y) {
        //This is a single result situation...no algorithm was used
        if(x.getMatchingAlgorithmResult() == null){
            return 0;
        }
        if (!((x.getMatchingAlgorithmResult() < y.getMatchingAlgorithmResult() || (x.getMatchingAlgorithmResult() == y.getMatchingAlgorithmResult() && x.getMatchingScore() > y.getMatchingScore())))) {
            return -1;
        }
        return 0;
    }
}
