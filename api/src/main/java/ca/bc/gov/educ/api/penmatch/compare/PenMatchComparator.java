package ca.bc.gov.educ.api.penmatch.compare;

import java.util.Comparator;

import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchRecord;

public class PenMatchComparator implements Comparator<PenMatchRecord> {
    @Override
    public int compare(PenMatchRecord x, PenMatchRecord y) {
        if (!((x.getMatchingAlgorithmResult() < y.getMatchingAlgorithmResult() || (x.getMatchingAlgorithmResult() == y.getMatchingAlgorithmResult() && x.getMatchingScore() > y.getMatchingScore())))) {
            return -1;
        }
        return 0;
    }
}
