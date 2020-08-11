package ca.bc.gov.educ.api.penmatch.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@Table(name = "NICKNAMES@penlink.world")
@IdClass(NicknamesEntity.class)
public class NicknamesEntity implements Serializable {

	@Id
	@Column(name = "NICKNAME1")
	private String nickname1;
	@Id
	@Column(name = "NICKNAME2")
	private String nickname2;

}
