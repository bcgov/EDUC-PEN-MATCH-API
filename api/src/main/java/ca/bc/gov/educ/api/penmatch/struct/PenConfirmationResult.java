package ca.bc.gov.educ.api.penmatch.struct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PenConfirmationResult {
	public static final String PEN_CONFIRMED = "PEN_CONFIRMED";
	public static final String PEN_ON_FILE = "PEN_ON_FILE";

	private String mergedPEN;
	private boolean deceased;
	private String penConfirmationResultCode;
	private PenMasterRecord masterRecord;
	private String localStudentNumber;
}
