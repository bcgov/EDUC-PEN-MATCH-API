package ca.bc.gov.educ.api.penmatch.struct;

import ca.bc.gov.educ.api.penmatch.enumeration.PenAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PenConfirmationResult {
	public static final String PEN_CONFIRMED = "PEN_CONFIRMED";
	public static final String PEN_ON_FILE = "PEN_ON_FILE";

	private String mergedPEN;
	private String resultCode;
	private PenMasterRecord penMasterRecord;
	private String localStudentNumber;
	private PenAlgorithm algorithmUsed;
}
