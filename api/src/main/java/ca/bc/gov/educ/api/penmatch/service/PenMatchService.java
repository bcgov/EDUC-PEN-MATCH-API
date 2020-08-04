package ca.bc.gov.educ.api.penmatch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.exception.PENMatchRuntimeException;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenConfirmationResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PenMatchService {

	private PenMatchNames penMatchNames;

	public static final String CHECK_DIGIT_ERROR_CODE_000 = "000";
	public static final String CHECK_DIGIT_ERROR_CODE_001 = "001";
	public static final String PEN_STATUS_AA = "AA";
	public static final String PEN_STATUS_B = "B";
	public static final String PEN_STATUS_B1 = "B1";
	public static final String PEN_STATUS_C = "C";
	public static final String PEN_STATUS_C0 = "C0";
	public static final String PEN_STATUS_C1 = "C1";
	public static final String PEN_STATUS_D = "D";
	public static final String PEN_STATUS_D0 = "D0";
	public static final String PEN_STATUS_D1 = "D1";
	public static final String PEN_STATUS_F = "F";
	public static final String PEN_STATUS_F1 = "F1";
	public static final String PEN_STATUS_G0 = "G0";
	public static final String ALGORITHM_00 = "00";
	public static final String ALGORITHM_S1 = "S1";
	public static final String ALGORITHM_S2 = "S2";
	public static final Integer VERY_FREQUENT = 500;
	public static final Integer NOT_VERY_FREQUENT = 50;
	public static final Integer VERY_RARE = 5;
	private List<String> matchingPENs;
	private Integer reallyGoodMatches;
	private Integer prettyGoodMatches;
	private String reallyGoodPEN;
	private String localStudentNumber;
	private boolean type5Match;
	private boolean type5F1;
	private boolean penFoundOnMaster;
	private boolean matchFound;
	private String alternateLocalID;
	private String studentSurnameNoBlanks;
	private String usualSurnameNoBlanks;
	private Integer minSurnameSearchSize;
	private Integer maxSurnameSearchSize;
	private Integer surnameSize;
	private Integer fullSurnameFrequency;
	private String fullStudentSurname;
	private Integer partSurnameFrequency;
	private String algorithmUsed;
	private Integer sexPoints;
	private boolean useGivenInitial;
	private String partStudentSurname;
	private String partStudentGiven;
	private String wyPEN;

	@Getter(AccessLevel.PRIVATE)
	private final PenDemographicsRepository penDemographicsRepository;

	@Autowired
	public PenMatchService(final PenDemographicsRepository penDemographicsRepository) {
		this.penDemographicsRepository = penDemographicsRepository;
	}

	public PenMatchStudent matchStudent(PenMatchStudent student) {
		log.info("Received student payload :: {}", student);

		initialize(student);

		if (student.getStudentNumber() != null) {
			String checkDigitErrorCode = penCheckDigit(student.getStudentNumber());
			if (checkDigitErrorCode != null) {
				if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_000)) {
					PenConfirmationResult penConfirmation = confirmPEN(student);
					if (penConfirmation.getResultCode() == PenConfirmationResult.PEN_CONFIRMED) {
						if (penConfirmation.getMergedPEN() == null) {
							student.setPenStatus(PEN_STATUS_AA);
							student.setStudentNumber(penConfirmation.getPenMasterRecord().getMasterStudentNumber());
						} else {
							student.setPenStatus(PEN_STATUS_B1);
							student.setStudentNumber(penConfirmation.getMergedPEN());
							student.setPen1(penConfirmation.getMergedPEN());
							student.setNumberOfMatches(1);
						}
					} else if (penConfirmation.getResultCode() == PenConfirmationResult.PEN_ON_FILE) {
						student.setPenStatus(PEN_STATUS_B);
						if (penConfirmation.getPenMasterRecord().getMasterStudentNumber() != null) {
							penFoundOnMaster = true;
						}
						findMatchesOnPenDemog(student);
					} else {
						student.setPenStatus(PEN_STATUS_C);
						findMatchesOnPenDemog(student);
					}

				} else if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_001)) {
					student.setPenStatus(PEN_STATUS_C);
					findMatchesOnPenDemog(student);
				}
			}
		} else {
			student.setPenStatus(PEN_STATUS_D);
			findMatchesOnPenDemog(student);
		}

		/*
		 * Assign a new PEN for status C0 or D0 unless Special Match or Search
		 * t_update_code values: Y or R - Assign new PEN (NO LONGER DONE HERE - NOW
		 * ASSIGNED IN ASSIGN_NEW_PEN.USE) N - Match only, do not assign new PEN S -
		 * Search only, do not assign new PEN
		 * 
		 * NOTE: Logic will need to be inserted to check to see if the new PEN exists
		 * once PEN 222222226 is reached. That PEN number as well as PENs starting with
		 * digits 3-9 have already been assigned in an earlier version of PEN.
		 */
		if ((student.getPenStatus() == PEN_STATUS_C0 || student.getPenStatus() == PEN_STATUS_D0)
				&& (student.getUpdateCode() == "Y" || student.getUpdateCode() == "R")) {
			checkForCoreData(student);
		}

		if (student.getPenStatus() == PEN_STATUS_AA || student.getPenStatus() == PEN_STATUS_B1
				|| student.getPenStatus() == PEN_STATUS_C1 || student.getPenStatus() == PEN_STATUS_D1) {
			PenMasterRecord penMasterRecord = getPENDemogMasterRecord(student.getStudentNumber());
			if (penMasterRecord.getMasterStudentDob() != student.getDob()) {
				student.setPenStatusMessage(
						"Birthdays are suspect: " + penMasterRecord.getMasterStudentDob() + " vs " + student.getDob());
				student.setPenStatus(PEN_STATUS_F1);
				student.setPen1(student.getStudentNumber());
				student.setStudentNumber(null);
			}

			if (penMasterRecord.getMasterStudentSurname() == student.getSurname()
					&& penMasterRecord.getMasterStudentGiven() != student.getGivenName()
					&& penMasterRecord.getMasterStudentDob() == student.getDob()
					&& penMasterRecord.getMasterPenMincode() == student.getMincode()
					&& penMasterRecord.getMasterPenLocalId() != student.getLocalID()
					&& penMasterRecord.getMasterPenLocalId() != null && student.getLocalID() != null) {
				student.setPenStatusMessage("Possible twin: " + penMasterRecord.getMasterStudentGiven().trim() + " vs "
						+ student.getGivenName().trim());
				student.setPenStatus(PEN_STATUS_F1);
				student.setPen1(student.getStudentNumber());
				student.setStudentNumber(null);
			}
		}

		if (student.isDeceased()) {
			student.setPenStatus(PEN_STATUS_C0);
			student.setStudentNumber(null);
		}

		return null;
	}

	private void initialize(PenMatchStudent student) {
		student.setPenStatusMessage(null);
		this.matchingPENs = new ArrayList<String>();
		this.localStudentNumber = null;

		this.reallyGoodMatches = 0;
		this.prettyGoodMatches = 0;
		this.reallyGoodPEN = null;

		student.setNumberOfMatches(0);
		this.type5Match = false;
		this.type5F1 = false;
		this.penFoundOnMaster = false;
		this.useGivenInitial = false;
		this.alternateLocalID = "TTT";

		// Strip off leading zeros, leading blanks and trailing blanks
		// from the local_id. Put result in alternateLocalID.
		if (student.getLocalID() != null) {
			alternateLocalID = StringUtils.stripStart(student.getLocalID(), "0");
			alternateLocalID = alternateLocalID.replaceAll(" ", "");
		}

		// Remove blanks from names
		if (student.getSurname() != null) {
			this.studentSurnameNoBlanks = student.getSurname().replaceAll(" ", "");
			// Re-calculate Soundex of legal surname
			student.setSoundexSurname(runSoundex(this.studentSurnameNoBlanks));
		}

		if (student.getUsualSurname() != null) {
			this.usualSurnameNoBlanks = student.getUsualSurname().replaceAll(" ", "");
			// Re-calculate Soundex of usual surname
			student.setUsualSoundexSurname(runSoundex(this.usualSurnameNoBlanks));
		}

		// Store given and middle names from transaction in separate object
		storeNamesFromTransaction(student);

		this.minSurnameSearchSize = 4;
		this.maxSurnameSearchSize = 6;

		if (student.getSurname() != null) {
			this.surnameSize = student.getSurname().length();
		} else {
			this.surnameSize = 0;
		}

		if (this.surnameSize < this.minSurnameSearchSize) {
			this.minSurnameSearchSize = this.surnameSize;
		} else if (this.surnameSize < this.maxSurnameSearchSize) {
			this.maxSurnameSearchSize = this.surnameSize;
		}

		// Lookup surname frequency
		// It could generate extra points later if
		// there is a perfect match on surname
		this.fullSurnameFrequency = 0;
		this.fullStudentSurname = student.getSurname();
		this.fullSurnameFrequency = lookupSurnameFrequency();

		if (this.fullSurnameFrequency > VERY_FREQUENT) {
			this.partSurnameFrequency = this.fullSurnameFrequency;
		} else {
			this.partSurnameFrequency = 0;
			this.fullStudentSurname = student.getSurname().substring(0, this.minSurnameSearchSize);
			this.partSurnameFrequency = lookupSurnameFrequency();
		}
	}

	/**
	 * This function stores all names in an object It includes some split logic for
	 * given/middle names
	 * 
	 * @param student
	 */
	private void storeNamesFromTransaction(PenMatchStudent student) {
		String given = student.getGivenName();
		String usualGiven = student.getUsualGivenName();

		this.penMatchNames = new PenMatchNames();
		this.penMatchNames.setLegalGiven(student.getGivenName());
		this.penMatchNames.setLegalMiddle(student.getMiddleName());
		this.penMatchNames.setUsualGiven(student.getUsualGivenName());
		this.penMatchNames.setUsualMiddle(student.getUsualMiddleName());

		if (given != null) {
			int spaceIndex = StringUtils.indexOf(given, " ");
			if (spaceIndex != -1) {
				this.penMatchNames.setAlternateLegalGiven(given.substring(0, spaceIndex));
				this.penMatchNames.setAlternateLegalMiddle(given.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(given, "-");
			if (dashIndex != -1) {
				this.penMatchNames.setAlternateLegalGiven(given.substring(0, dashIndex));
				this.penMatchNames.setAlternateLegalMiddle(given.substring(dashIndex));
			}
		}

		if (usualGiven != null) {
			int spaceIndex = StringUtils.indexOf(usualGiven, " ");
			if (spaceIndex != -1) {
				this.penMatchNames.setAlternateUsualGiven(usualGiven.substring(0, spaceIndex));
				this.penMatchNames.setAlternateUsualMiddle(usualGiven.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(usualGiven, "-");
			if (dashIndex != -1) {
				this.penMatchNames.setAlternateUsualGiven(usualGiven.substring(0, dashIndex));
				this.penMatchNames.setAlternateUsualMiddle(usualGiven.substring(dashIndex));
			}
		}

		lookupNicknames();
	}

	/**
	 * Example: the original PEN number is 746282656 1. First 8 digits are 74628265
	 * 2. Sum the odd digits: 7 + 6 + 8 + 6 = 27 (S1) 3. Extract the even digits
	 * 4,2,2,5 to get A = 4225. 4. Multiply A times 2 to get B = 8450 5. Sum the
	 * digits of B: 8 + 4 + 5 + 0 = 17 (S2) 6. 27 + 17 = 44 (S3) 7. S3 is not a
	 * multiple of 10 8. Calculate check-digit as 10 - MOD(S3,10): 10 - MOD(44,10) =
	 * 10 - 4 = 6 A) Alternatively, round up S3 to next multiple of 10: 44 becomes
	 * 50 B) Subtract S3 from this: 50 - 44 = 6
	 * 
	 * @param pen
	 * @return
	 */
	private String penCheckDigit(String pen) {
		if (pen == null || pen.length() != 9 || !pen.matches("-?\\d+(\\.\\d+)?")) {
			return CHECK_DIGIT_ERROR_CODE_001;
		}

		ArrayList<Integer> odds = new ArrayList<>();
		ArrayList<Integer> evens = new ArrayList<>();
		for (int i = 0; i < pen.length() - 1; i++) {
			int number = Integer.parseInt(pen.substring(i, i + 1));
			if (i % 2 == 0) {
				odds.add(number);
			} else {
				evens.add(number);
			}
		}

		int sumOdds = odds.stream().mapToInt(Integer::intValue).sum();

		String fullEvenValueString = "";
		for (int i = 0; i < evens.size(); i++) {
			fullEvenValueString += evens.get(i);
		}

		ArrayList<Integer> listOfFullEvenValueDoubled = new ArrayList<>();
		String fullEvenValueDoubledString = Integer.valueOf(Integer.parseInt(fullEvenValueString) * 2).toString();
		for (int i = 0; i < fullEvenValueDoubledString.length(); i++) {
			listOfFullEvenValueDoubled.add(Integer.parseInt(fullEvenValueDoubledString.substring(i, i + 1)));
		}

		int sumEvens = listOfFullEvenValueDoubled.stream().mapToInt(Integer::intValue).sum();

		int finalSum = sumEvens + sumOdds;

		String penCheckDigit = pen.substring(8, 9);

		if ((finalSum % 10 == 0 && penCheckDigit == "0") || ((10 - finalSum % 10) == Integer.parseInt(penCheckDigit))) {
			return CHECK_DIGIT_ERROR_CODE_000;
		} else {
			return CHECK_DIGIT_ERROR_CODE_001;
		}
	}

	private String runSoundex(String name) {
		// TODO Implement this
		return "";
	}

	private void checkForCoreData(PenMatchStudent student) {
		if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null
				|| student.getSex() == null || student.getMincode() == null) {
			student.setPenStatus(PEN_STATUS_G0);
		}
	}

	/**
	 * Check for exact match on surname , given name, birthday and gender OR exact
	 * match on school and local ID and one or more of surname, given name or
	 * birthday
	 */
	private void simpleCheckForMatch(PenMatchStudent student, PenMasterRecord master) {
		this.matchFound = false;
		this.type5Match = false;

		if (student.getSurname() != null && student.getSurname() == master.getMasterStudentSurname()
				&& student.getGivenName() != null && student.getGivenName() == master.getMasterStudentGiven()
				&& student.getDob() != null && student.getDob() == master.getMasterStudentDob()
				&& student.getSex() != null && student.getSex() == master.getMasterStudentSex()) {
			this.matchFound = true;
			this.algorithmUsed = ALGORITHM_S1;
		} else if (student.getSurname() != null && student.getSurname() == master.getMasterStudentSurname()
				&& student.getGivenName() != null && student.getGivenName() == master.getMasterStudentGiven()
				&& student.getDob() != null && student.getDob() == master.getMasterStudentDob()
				&& student.getLocalID() != null && student.getLocalID().length() > 1) {
			normalizeLocalIDsFromMaster(master);
			if (student.getMincode() != null && student.getMincode() == master.getMasterPenMincode()
					&& (student.getLocalID() == master.getMasterPenLocalId()
							|| this.alternateLocalID == master.getMasterAlternateLocalId())) {
				this.matchFound = true;
				this.algorithmUsed = ALGORITHM_S2;
			}
		}

		if (this.matchFound) {
			loadPenMatchHistory();
		}

	}

	/**
	 * Strip off leading zeros , leading blanks and trailing blanks from the
	 * PEN_MASTER stud_local_id. Put result in MAST_PEN_ALT_LOCAL_ID
	 */
	private void normalizeLocalIDsFromMaster(PenMasterRecord master) {
		master.setMasterAlternateLocalId("MMM");
		if (master.getMasterPenLocalId() != null) {
			master.setMasterAlternateLocalId(StringUtils.stripStart(master.getMasterPenLocalId(), "0"));
			master.setMasterAlternateLocalId(master.getMasterAlternateLocalId().replaceAll(" ", ""));
		}
	}

	private PenConfirmationResult confirmPEN(PenMatchStudent student) {
		PenConfirmationResult penConfirmationResult = new PenConfirmationResult();
		this.localStudentNumber = student.getStudentNumber();
		student.setDeceased(false);

		PenMasterRecord penMasterRecord = getPENDemogMasterRecord(this.localStudentNumber);

		if (penMasterRecord != null && penMasterRecord.getMasterStudentNumber() == this.localStudentNumber) {
			penConfirmationResult.setResultCode(PenConfirmationResult.PEN_ON_FILE);
			if (penMasterRecord.getMasterStudentStatus() == "M"
					&& penMasterRecord.getMasterStudentTrueNumber() != null) {
				this.localStudentNumber = penMasterRecord.getMasterStudentTrueNumber();
				penConfirmationResult.setMergedPEN(penMasterRecord.getMasterStudentTrueNumber());
				penMasterRecord = getPENDemogMasterRecord(this.localStudentNumber);
				if (penMasterRecord != null && penMasterRecord.getMasterStudentNumber() == this.localStudentNumber) {
					simpleCheckForMatch(student, penMasterRecord);
					if (penMasterRecord.getMasterStudentStatus() == "D") {
						this.localStudentNumber = null;
						student.setDeceased(true);
					}
				}
			} else {
				simpleCheckForMatch(student, penMasterRecord);
			}
			if (this.matchFound) {
				penConfirmationResult.setResultCode(PenConfirmationResult.PEN_CONFIRMED);
			}
		}

		if (this.matchFound) {
			loadPenMatchHistory();
		}

		return penConfirmationResult;
	}

	/**
	 * Find all possible students on master who could match the transaction - If the
	 * first four characters of surname are uncommon then only use 4 characters in
	 * lookup. Otherwise use 6 characters , or 5 if surname is only 5 characters
	 * long use the given initial in the lookup unless 1st 4 characters of surname
	 * is quite rare
	 */
	private void findMatchesOnPenDemog(PenMatchStudent student) {
		this.useGivenInitial = true;

		if (this.partSurnameFrequency <= NOT_VERY_FREQUENT) {
			this.partStudentSurname = student.getSurname().substring(0, this.minSurnameSearchSize);
			this.useGivenInitial = false;
		} else {
			if (this.partSurnameFrequency <= VERY_FREQUENT) {
				this.partStudentSurname = student.getSurname().substring(0, this.minSurnameSearchSize);
				this.partStudentGiven = student.getGivenName().substring(0, 1);
			} else {
				this.partStudentSurname = student.getSurname().substring(0, this.maxSurnameSearchSize);
				this.partStudentGiven = student.getGivenName().substring(0, 2);
			}
		}

		if (student.getLocalID() == null) {
			if (this.useGivenInitial) {
				lookupNoLocalID();
			} else {
				lookupNoInitNoLocalID();
			}
		} else {
			if (this.useGivenInitial) {
				lookupWithAllParts();
			} else {
				lookupNoInit();
			}
		}

//		If a PEN was provided, but the demographics didn't match the student
//		on PEN-MASTER with that PEN, then add the student on PEN-MASTER to 
//		the list of possible students who match.
		if (student.getPenStatus() == PEN_STATUS_B && this.penFoundOnMaster) {
			this.reallyGoodMatches = 0;
			this.wyPEN = this.localStudentNumber;
			this.algorithmUsed = ALGORITHM_00;
			this.type5F1 = true;
			mergeNewMatchIntoList();
		}

//		If only one really good match, and no pretty good matches,
//		just send the one PEN back
		if (student.getPenStatus().substring(0, 1) == PEN_STATUS_D && this.reallyGoodMatches == 1
				&& this.prettyGoodMatches == 0) {
			student.setPen1(this.reallyGoodPEN);
			student.setStudentNumber(this.reallyGoodPEN);
			student.setNumberOfMatches(1);
			student.setPenStatus(PEN_STATUS_D1);
			return;
		} else {
			log.debug("List of matching PENs: {}", matchingPENs);
			student.setPen1(matchingPENs.get(0));
			student.setPen2(matchingPENs.get(1));
			student.setPen3(matchingPENs.get(2));
			student.setPen4(matchingPENs.get(3));
			student.setPen5(matchingPENs.get(4));
			student.setPen6(matchingPENs.get(5));
			student.setPen7(matchingPENs.get(6));
			student.setPen8(matchingPENs.get(7));
			student.setPen9(matchingPENs.get(8));
			student.setPen10(matchingPENs.get(9));
			student.setPen11(matchingPENs.get(10));
			student.setPen12(matchingPENs.get(11));
			student.setPen13(matchingPENs.get(12));
			student.setPen14(matchingPENs.get(13));
			student.setPen15(matchingPENs.get(14));
			student.setPen16(matchingPENs.get(15));
			student.setPen17(matchingPENs.get(16));
			student.setPen18(matchingPENs.get(17));
			student.setPen19(matchingPENs.get(18));
			student.setPen20(matchingPENs.get(19));
		}

		if (student.getNumberOfMatches() == 0) {
			// No matches found
			student.setPenStatus(student.getPenStatus().trim() + "0");
			student.setStudentNumber(null);
		} else if (student.getNumberOfMatches() == 1) {
			// 1 match only
			if(this.type5F1) {
				student.setPenStatus(PEN_STATUS_F);
				student.setStudentNumber(null);
			}else {
				//one solid match, put in t_stud_no
				student.setStudentNumber(this.matchingPENs.get(0)); 
			}
			student.setPenStatus(student.getPenStatus().trim() + "1");
		}else {
			student.setPenStatus(student.getPenStatus().trim() + "M");
			//many matches, so they are all considered questionable, even if some are "solid"
			student.setStudentNumber(null); 
		}

	}

	/**
	 * Calculate points for Sex match
	 */
	private void matchSex(PenMatchStudent student, PenMasterRecord master) {
		this.sexPoints = 0;
		if (student.getSex() != null && student.getSex() == master.getMasterStudentSex()) {
			this.sexPoints = 5;
		}
	}

	/**
	 * Fetches a PEN Master Record given a student number
	 * 
	 * @param studentNumber
	 * @return
	 */
	private PenMasterRecord getPENDemogMasterRecord(String studentNumber) {
		Optional<PenDemographicsEntity> demog = getPenDemographicsRepository().findByStudNo(studentNumber);
		if (demog.isPresent()) {
			PenDemographicsEntity entity = demog.get();
			PenMasterRecord masterRecord = new PenMasterRecord();
			masterRecord.setMasterStudentNumber(entity.getStudNo());
			masterRecord.setMasterStudentSurname(entity.getStudSurname());
			masterRecord.setMasterStudentGiven(entity.getStudGiven());
			masterRecord.setMasterStudentDob(entity.getStudBirth());
			masterRecord.setMasterStudentSex(entity.getStudSex());
			masterRecord.setMasterStudentGrade(entity.getGrade());
			masterRecord.setMasterPenMincode(entity.getMincode());
			masterRecord.setMasterPenLocalId(entity.getLocalID());
			masterRecord.setMasterStudentStatus(entity.getStudStatus());
			masterRecord.setMasterStudentTrueNumber(entity.getTrueNumber());
			return masterRecord;
		}

		throw new PENMatchRuntimeException("No PEN Demog master record found for student number: " + studentNumber);
	}
	

	/**
	 * Create a log entry for analytical purposes. Not used in our Java
	 * implementation
	 */
	private void loadPenMatchHistory() {
		// Not currently implemented
		// This was a logging function in Basic, we'll likely do something different
	}

	private void mergeNewMatchIntoList() {
		// Not currently implemented
	}

	private void lookupWithAllParts() {
		// Not currently implemented
	}

	private void lookupNoInit() {
		// Not currently implemented
	}

	private void lookupNoLocalID() {
		// Not currently implemented
	}

	private void lookupNoInitNoLocalID() {
		// Not currently implemented
	}
	

	private void lookupNicknames() {
		// TODO Implement this
	}

	private Integer lookupSurnameFrequency() {
		// TODO Implement this
		// Note this returns in two different places
		return 0;
	}

}
