package ca.bc.gov.educ.api.penmatch.struct.v1;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PenMasterRecord {
	private UUID studentID;
	private String archiveFlag;
	private String pen;
	private String surname;
	private String given;
	private String middle;
	private String usualSurname;
	private String usualGivenName;
	private String usualMiddleName;
	private String provinceCode;
	private String countryCode;
	private String postal;
	private String dob;
	private String sex;
	private String grade;
	private String citizenship;
	private String status;
	private String homeLanguage;
	private String aboriginalIndicator;
	private String bandCode;
	private String mergedFromPEN;
	private String mincode;
	private String localId;

	private String alternateLocalId;
}
