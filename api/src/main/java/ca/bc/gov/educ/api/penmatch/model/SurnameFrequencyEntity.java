package ca.bc.gov.educ.api.penmatch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
