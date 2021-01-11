package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Match reason code.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchReasonCode {


  /**
   * The Match reason code.
   */
  String matchReasonCode;

  /**
   * The Label.
   */
  String label;

  /**
   * The Description.
   */
  String description;

  /**
   * The Display order.
   */
  Integer displayOrder;

  /**
   * The Effective date.
   */
  String effectiveDate;

  /**
   * The Expiry date.
   */
  String expiryDate;

  /**
   * The Create user.
   */
  String createUser;

  /**
   * The Create date.
   */
  String createDate;

  /**
   * The Update user.
   */
  String updateUser;

  /**
   * The Update date.
   */
  String updateDate;
}
