package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * The type New pen match session.
 */
@Data
@NoArgsConstructor
public class NewPenMatchSession {

  /**
   * The Matching records list.
   */
  private List<NewPenMatchRecord> matchingRecordsList;
  /**
   * The Matching records queue.
   */
  private PriorityQueue<BestMatchRecord> matchingRecordsQueue;
  /**
   * The Pen status.
   */
  private String penStatus;
  /**
   * The Pen status message.
   */
  private String penStatusMessage;
  /**
   * The Is psi.
   */
  private boolean isPSI;

  /**
   * correlation id to pass to different events for easy tracking.
   */
  private UUID correlationID;
}
