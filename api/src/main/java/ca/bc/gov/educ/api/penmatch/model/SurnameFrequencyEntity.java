package ca.bc.gov.educ.api.penmatch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "SURNAME_FREQUENCY")
public class SurnameFrequencyEntity {

	@Id
	@Column(name = "SURNAME")
	private String surname;

	@Column(name = "SURNAME_FREQUENCY")
	private String surnameFrequency;

}
