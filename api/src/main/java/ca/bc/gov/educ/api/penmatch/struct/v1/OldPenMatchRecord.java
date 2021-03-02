package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import lombok.*;

/**
 * The type Old pen match record.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OldPenMatchRecord extends PenMatchRecord {
  /**
   * The Matching algorithm result.
   */
  private Integer matchingAlgorithmResult;
  /**
   * The Matching score.
   */
  private Integer matchingScore;
  /**
   * The Master record.
   */
  private PenMasterRecord masterRecord;

  /**
   * Instantiates a new Old pen match record.
   *
   * @param matchingAlgorithmResult the matching algorithm result
   * @param matchingScore           the matching score
   * @param matchingPEN             the matching pen
   * @param studentID               the student id
   */
  @Builder
  public OldPenMatchRecord(Integer matchingAlgorithmResult, Integer matchingScore, String matchingPEN, String studentID, PenMasterRecord masterRecord) {
    super(matchingPEN, studentID);
    this.matchingAlgorithmResult = matchingAlgorithmResult;
    this.matchingScore = matchingScore;
    this.masterRecord = masterRecord;
  }
}
