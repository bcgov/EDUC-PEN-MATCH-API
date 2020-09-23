package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class PenMatchStudent {
	protected String pen;
	@Pattern(regexp = "^((19|20)\\d\\d)(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$")
	@NotNull(message = "Date of Birth can not be null.")
	protected String dob;
	@NotNull(message = "Sex Code can not be null.")
	protected String sex;
	protected String enrolledGradeCode;
	@Size(max = 255)
	@NotNull(message = "Surname can not be null.")
	protected String surname;
	@Size(max = 255)
	@NotNull(message = "Given name can not be null.")
	protected String givenName;
	protected String middleName;
	protected String usualSurname;
	protected String usualGivenName;
	protected String usualMiddleName;
	protected String mincode;
	protected String localID;
	protected String postal;
	protected String updateCode;
	protected String applicationCode;
	protected boolean assignNewPEN;

}
