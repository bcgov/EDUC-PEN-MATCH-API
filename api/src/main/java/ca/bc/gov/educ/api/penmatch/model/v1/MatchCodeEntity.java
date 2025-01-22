package ca.bc.gov.educ.api.penmatch.model.v1;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Match codes entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "MATCH_CODE")
@IdClass(MatchCodeEntity.class)
public class MatchCodeEntity implements Serializable {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = -8918085130403633012L;

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "MATCH_CODE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID matchCodeID;

  /**
   * The Match code.
   */
  @Column(name = "MATCH_CODE")
  private String matchCode;
  /**
   * The Match result.
   */
  @Column(name = "MATCH_RESULT")
  private String matchResult;

  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE")
  private LocalDateTime createDate;

  /**
   * The Create user name.
   */
  @Column(name = "CREATE_USER")
  private String createUser;

  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;

  /**
   * The Update user name.
   */
  @Column(name = "UPDATE_USER")
  private String updateUser;
}
