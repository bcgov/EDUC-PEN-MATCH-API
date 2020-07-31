package ca.bc.gov.educ.api.penmatch.service;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.struct.PenConfirmationResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
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
	public static final String PEN_STATUS_F1 = "F1";
	public static final Integer VERY_FREQUENT = 500;
	public static final Integer NOT_VERY_FREQUENT = 50;
	public static final Integer VERY_RARE = 5;
	private HashSet<String> matchingPENs;
	private Integer reallyGoodMatches;
	private Integer prettyGoodMatches;
	private Integer reallyGoodPEN;
	private String studentNumber;
	private boolean type5Match;
	private boolean type5F1;
	private boolean penFoundOnMaster;
	private String alternateLocalID;
	private String studentSurnameNoBlanks;
	private String usualSurnameNoBlanks;
	private Integer minSurnameSearchSize;
	private Integer maxSurnameSearchSize;
	private Integer surnameSize;
	private Integer fullSurnameFrequency;
	private String fullStudentSurname;
	private Integer partSurnameFrequency;
	private PenMasterRecord penMasterRecord;

	public PenMatchStudent matchStudent(PenMatchStudent student) {
		log.info("Received student payload :: {}", student);

		initialize(student);

		if (student.getStudentNumber() != null) {
			String checkDigitErrorCode = penCheckDigit(student.getStudentNumber());
			if (checkDigitErrorCode != null) {
				if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_000)) {
					PenConfirmationResult penConfirmation = confirmPEN();
					if (penConfirmation.getResultCode() == PenConfirmationResult.PEN_CONFIRMED) {
						if (penConfirmation.getMergedPEN() == null) {
							student.setPenStatus(PEN_STATUS_AA);
							student.setStudentNumber(penMasterRecord.getMasterStudentNumber());
						} else {
							student.setPenStatus(PEN_STATUS_B1);
							student.setStudentNumber(penConfirmation.getMergedPEN());
							student.setPen1(penConfirmation.getMergedPEN());
							student.setNoMatches(1);
						}
					} else if (penConfirmation.getResultCode() == PenConfirmationResult.PEN_ON_FILE) {
						student.setPenStatus(PEN_STATUS_B);
						if (penMasterRecord.getMasterStudentNumber() != null) {
							penFoundOnMaster = true;
						}
						findMatchesOnPenDemog();
					} else {
						student.setPenStatus(PEN_STATUS_C);
						findMatchesOnPenDemog();
					}

				} else if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_001)) {
					student.setPenStatus(PEN_STATUS_C);
					findMatchesOnPenDemog();
				}
			}
		} else {
			student.setPenStatus(PEN_STATUS_D);
			findMatchesOnPenDemog();
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
			checkForCodeData();
		}

		if (student.getPenStatus() == PEN_STATUS_AA || student.getPenStatus() == PEN_STATUS_B1
				|| student.getPenStatus() == PEN_STATUS_C1 || student.getPenStatus() == PEN_STATUS_D1) {
			penMasterRecord = getPENMasterRecord(student.getStudentNumber());
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
					&& penMasterRecord.getMasterPenLocalId() != null 
					&& student.getLocalID() != null) {
				student.setPenStatusMessage("Possible twin: " + penMasterRecord.getMasterStudentGiven().trim() + " vs "
						+ student.getGivenName().trim());
				student.setPenStatus(PEN_STATUS_F1);
				student.setPen1(student.getStudentNumber());
				student.setStudentNumber(null);
			}
		}
		
		if(student.isDeceased()) {
			student.setPenStatus(PEN_STATUS_C0);
			student.setStudentNumber(null);
		}

		return null;
	}

	private void initialize(PenMatchStudent student) {
		student.setPenStatusMessage(null);
		this.matchingPENs = new HashSet<String>();
		studentNumber = null;

		this.reallyGoodMatches = 0;
		this.prettyGoodMatches = 0;
		this.reallyGoodPEN = null;

		student.setNoMatches(0);
		this.type5Match = false;
		this.type5F1 = false;
		this.penFoundOnMaster = false;
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

	private void checkForCodeData() {
		// TODO Implement this
	}

	private void lookupNicknames() {
		// TODO Implement this
	}

	private Integer lookupSurnameFrequency() {
		// TODO Implement this
		// Note this returns in two different places
		return 0;
	}

	private PenConfirmationResult confirmPEN() {
		penMasterRecord = new PenMasterRecord();
		return new PenConfirmationResult();
	}

	private void findMatchesOnPenDemog() {
		// TODO Implement this
	}

	private PenMasterRecord getPENMasterRecord(String studentNumber) {
		return new PenMasterRecord();
	}
}
