package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * The type Nicknames entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "NICKNAMES")
@IdClass(NicknamesEntity.class)
public class NicknamesEntity implements Serializable {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = -8918085130403633012L;
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

}
