package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The type Match codes entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "MATCH_CODES")
@IdClass(MatchCodesEntity.class)
public class MatchCodesEntity implements Serializable {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = -8918085130403633012L;
  /**
   * The Match code.
   */
  @Id
  @Column(name = "MATCH_CODE")
  private String matchCode;
  /**
   * The Match result.
   */
  @Id
  @Column(name = "MATCH_RESULT")
  private String matchResult;

}
