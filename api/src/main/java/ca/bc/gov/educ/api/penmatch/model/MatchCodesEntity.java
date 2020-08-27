package ca.bc.gov.educ.api.penmatch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@Immutable
@Table(name = "MATCH_CODES")
@IdClass(MatchCodesEntity.class)
public class MatchCodesEntity implements Serializable {

	private static final long serialVersionUID = -8918085130403633012L;
	@Id
	@Column(name = "MATCH_CODE")
	private String matchCode;
	@Id
	@Column(name = "MATCH_RESULT")
	private String matchResult;

}
