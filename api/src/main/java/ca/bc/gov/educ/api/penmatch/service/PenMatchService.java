package ca.bc.gov.educ.api.penmatch.service;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PenMatchService {
	
	private PenMatchNames penMatchNames;

	public static final String CHECK_DIGIT_ERROR_CODE_000 = "000";
	public static final String CHECK_DIGIT_ERROR_CODE_001 = "001";
	public static final String PEN_STATUS_C = "C";
	public static final String PEN_STATUS_D = "D";
	
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

	public PenMatchStudent matchStudent(PenMatchStudent student) {
		log.info("Received student payload :: {}", student);

		initialize(student);

		if (student.getStudentNumber() != null) {
			String checkDigitErrorCode = penCheckDigit(student.getStudentNumber());
			if (checkDigitErrorCode != null) {
				if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_000)) {
					confirmPEN();

				} else if (checkDigitErrorCode.equals(CHECK_DIGIT_ERROR_CODE_001)) {
					student.setPenStatus(PEN_STATUS_C);
				}
			}
		} else {
			student.setPenStatus(PEN_STATUS_D);
			findMatchesOnPenDemog();
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
		
		//Store given and middle names from transaction in separate object
		storeNamesFromTransaction(student);
		
		this.minSurnameSearchSize = 4;
		this.maxSurnameSearchSize = 6;
		
		if(student.getSurname() != null) {
			this.surnameSize = student.getSurname().length();	
		}else {
			this.surnameSize = 0;
		}
		
		if(this.surnameSize < this.minSurnameSearchSize) {
			this.minSurnameSearchSize = this.surnameSize;
		}else if(this.surnameSize < this.maxSurnameSearchSize) {
			this.maxSurnameSearchSize = this.surnameSize;
		}
		
		//Lookup surname frequency
		//It could generate extra points later if
		//there is a perfect match on surname
		this.fullSurnameFrequency = 0;
		this.fullStudentSurname = student.getSurname();
		this.fullSurnameFrequency = lookupSurnameFrequency();
		
		if(this.fullSurnameFrequency > VERY_FREQUENT) {
			this.partSurnameFrequency = this.fullSurnameFrequency;
		}else {
			this.partSurnameFrequency = 0;
			this.fullStudentSurname = student.getSurname().substring(0, this.minSurnameSearchSize);
			this.partSurnameFrequency = lookupSurnameFrequency();
		}
	}

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
		//TODO Implement this
		return "";
	}

	private String runSoundex(String name) {
		//TODO Implement this
		return "";
	}
	
	private void lookupNicknames() {
		//TODO Implement this
	}

	private Integer lookupSurnameFrequency() {
		//TODO Implement this
		//Note this returns in two different places
		return 0; 
	}
}
