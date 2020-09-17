package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PenMatchStudent {
	protected String pen;
	protected String dob;
	protected String sex;
	protected String enrolledGradeCode;
	protected String surname;
	protected String givenName;
	protected String middleName;
	protected String usualSurname;
	protected String usualGivenName;
	protected String usualMiddleName;
	protected String mincode;
	protected String localID;
	protected String postal;
	protected String updateCode;
}
