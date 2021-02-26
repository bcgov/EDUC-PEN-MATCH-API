package ca.bc.gov.educ.api.penmatch.compare;

import ca.bc.gov.educ.api.penmatch.struct.v1.OldPenMatchRecord;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

/**
 * The type Pen match comparator.
 */
public class PenMatchComparator implements Comparator<OldPenMatchRecord> {
  @Override
  public int compare(OldPenMatchRecord x, OldPenMatchRecord y) {
    //This is a single result situation...no algorithm was used
    if (x.getMatchingAlgorithmResult() == null) {
      return 0;
    }
    if (!(x.getMatchingAlgorithmResult().intValue() < y.getMatchingAlgorithmResult().intValue()
        || (x.getMatchingAlgorithmResult().equals(y.getMatchingAlgorithmResult()) && x.getMatchingScore().intValue() > y.getMatchingScore().intValue()))) {
      return -1;
    }
    return 1;
  }
}
