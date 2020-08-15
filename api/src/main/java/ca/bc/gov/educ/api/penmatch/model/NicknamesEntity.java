package ca.bc.gov.educ.api.penmatch.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "NICKNAMES")
@IdClass(NicknamesEntity.class)
public class NicknamesEntity implements Serializable {

	private static final long serialVersionUID = -8918085130403633012L;
	@Id
	@Column(name = "NICKNAME1")
	private String nickname1;
	@Id
	@Column(name = "NICKNAME2")
	private String nickname2;

}
