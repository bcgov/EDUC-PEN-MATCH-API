package ca.bc.gov.educ.api.penmatch.compare;

import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.BestMatchRecord;

import java.util.Comparator;

public class NewPenMatchComparator implements Comparator<BestMatchRecord> {
    @Override
    public int compare(BestMatchRecord x, BestMatchRecord y) {
        //This is a single result situation...no algorithm was used
        if(x.getMatchValue() == null){
            return 0;
        }
        if (x.getMatchValue() > y.getMatchValue()) {
            return -1;
        }

        return 0;
    }
}
