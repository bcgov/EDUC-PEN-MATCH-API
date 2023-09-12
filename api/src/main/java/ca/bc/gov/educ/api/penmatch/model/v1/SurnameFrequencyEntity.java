package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The type Surname frequency entity.
 */
@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "SURNAME_FREQUENCY")
public class SurnameFrequencyEntity {

  /**
   * The Surname.
   */
  @Id
  @Column(name = "SURNAME")
  private String surname;

  /**
   * The Surname frequency.
   */
  @Column(name = "SURNAME_FREQUENCY")
  private String surnameFrequency;

}
