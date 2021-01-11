package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * The type Foreign surnames entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "FOREIGN_SURNAMES")
public class ForeignSurnamesEntity {

  /**
   * The Surname.
   */
  @Id
  @Column(name = "SURNAME")
  private String surname;

  /**
   * The Ancestry.
   */
  @Column(name = "ANCESTRY")
  private String ancestry;

  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE")
  private LocalDateTime createDate;

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
   * The Create user name.
   */
  @Column(name = "CREATE_USER_NAME")
  private String createUserName;

  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;

  /**
   * The Update user name.
   */
  @Column(name = "UPDATE_USER_NAME")
  private String updateUserName;
}
