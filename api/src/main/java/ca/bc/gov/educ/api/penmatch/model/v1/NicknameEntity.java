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
 * The type Nicknames entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "NICKNAME")
@IdClass(NicknameEntity.class)
public class NicknameEntity implements Serializable {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = -8918085130403633012L;
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "NICKNAME_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID nicknameID;
  /**
   * The Nickname 1.
   */
  @Id
  @Column(name = "NICKNAME1")
  private String nickname1;
  /**
   * The Nickname 2.
   */
  @Id
  @Column(name = "NICKNAME2")
  private String nickname2;

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
