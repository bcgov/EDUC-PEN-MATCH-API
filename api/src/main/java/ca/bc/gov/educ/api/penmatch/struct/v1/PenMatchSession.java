package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.PriorityQueue;

/**
 * The type Pen match session.
 */
@Data
@NoArgsConstructor
public class PenMatchSession {
  /**
   * The Matching records.
   */
  private PriorityQueue<OldPenMatchRecord> matchingRecords;
  /**
   * The Pretty good match record.
   */
  private PenMasterRecord prettyGoodMatchRecord;
  /**
   * The Really good master record.
   */
  private PenMasterMatchedRecord reallyGoodMasterMatchRecord;
  /**
   * The Pen status.
   */
  private String penStatus;
  /**
   * The Pen status message.
   */
  private String penStatusMessage;
  /**
   * The Pen status message.
   */
  private boolean type5F1;

}
