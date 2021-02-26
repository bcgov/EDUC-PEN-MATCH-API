package ca.bc.gov.educ.api.penmatch.compare;

import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.BestMatchRecord;

import java.util.Comparator;

/**
 * The type New pen match comparator.
 */
public class NewPenMatchComparator implements Comparator<BestMatchRecord> {
  @Override
  public int compare(BestMatchRecord x, BestMatchRecord y) {
    //This is a single result situation...no algorithm was used
    if (x.getMatchValue() == null) {
      return 0;
    }

    if (x.getMatchValue().equals(y.getMatchValue())) {
      if(Integer.parseInt(x.getMatchPEN()) < Integer.parseInt(y.getMatchPEN())){
        return -1;
      }
      return 1;
    }

    if (x.getMatchValue().longValue() < y.getMatchValue().longValue()) {
      return -1;
    }

    return 1;
  }
}
