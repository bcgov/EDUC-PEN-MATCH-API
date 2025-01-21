package ca.bc.gov.educ.api.penmatch.model.v1;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Foreign surnames entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "FOREIGN_SURNAME")
public class ForeignSurnameEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "FOREIGN_SURNAME_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID foreignSurnameID;
  /**
   * The Surname.
   */
  @Column(name = "SURNAME")
  private String surname;

  /**
   * The Ancestry.
   */
  @Column(name = "ANCESTRY")
  private String ancestry;

  /**
   * The Effective date.
   */
  @Column(name = "EFFECTIVE_DATE")
  private LocalDateTime effectiveDate;

  /**
   * The Expiry date.
   */
  @Column(name = "EXPIRY_DATE")
  private LocalDateTime expiryDate;

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
