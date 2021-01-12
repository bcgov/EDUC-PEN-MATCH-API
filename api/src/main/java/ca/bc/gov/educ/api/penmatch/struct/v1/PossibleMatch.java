package ca.bc.gov.educ.api.penmatch.struct.v1;

import ca.bc.gov.educ.api.penmatch.constants.MatchReasonCodes;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * The type Possible match.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PossibleMatch {
  /**
   * The Possible match id.
   */
  String possibleMatchID;
  /**
   * The Student id.
   */
  @NotNull
  String studentID;
  /**
   * The Matched student id.
   */
  @NotNull
  String matchedStudentID;
  /**
   * The Match reason code.
   */
  @NotNull
  MatchReasonCodes matchReasonCode;
  /**
   * The Create user.
   */
  @NotNull
  String createUser;
  /**
   * The Update user.
   */
  @NotNull
  String updateUser;

  /**
   * The Create date.
   */
  @Null
  String createDate;

  /**
   * The Update date.
   */
  @Null
  String updateDate;
}