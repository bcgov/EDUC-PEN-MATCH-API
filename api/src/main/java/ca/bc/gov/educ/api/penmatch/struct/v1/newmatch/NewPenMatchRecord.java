package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type New pen match record.
 */
@Getter
@Setter
@AllArgsConstructor
public class NewPenMatchRecord extends PenMatchRecord {
  /**
   * The Match result.
   */
  private String matchResult;
  /**
   * The Match code.
   */
  private String matchCode;

  /**
   * Instantiates a new New pen match record.
   *
   * @param matchResult the match result
   * @param matchCode   the match code
   * @param matchingPEN the matching pen
   * @param studentID   the student id
   */
  @Builder
  public NewPenMatchRecord(String matchResult, String matchCode, String matchingPEN, String studentID) {
    super(matchingPEN, studentID);
    this.matchResult = matchResult;
    this.matchCode = matchCode;
  }
}
