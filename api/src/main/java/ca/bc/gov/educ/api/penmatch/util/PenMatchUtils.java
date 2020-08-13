package ca.bc.gov.educ.api.penmatch.util;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

public class PenMatchUtils {

	/**
	 * Utility method which sets the penMatchTransactionNames
	 * 
	 * @param penMatchTransactionNames
	 * @param nextNickname
	 */
	public static void setNextNickname(PenMatchNames penMatchTransactionNames, String nextNickname) {
		if (penMatchTransactionNames.getNickname1() == null || penMatchTransactionNames.getNickname1().length() < 1) {
			penMatchTransactionNames.setNickname1(nextNickname);
		} else if (penMatchTransactionNames.getNickname2() == null || penMatchTransactionNames.getNickname2().length() < 1) {
			penMatchTransactionNames.setNickname2(nextNickname);
		} else if (penMatchTransactionNames.getNickname3() == null || penMatchTransactionNames.getNickname3().length() < 1) {
			penMatchTransactionNames.setNickname3(nextNickname);
		} else if (penMatchTransactionNames.getNickname4() == null || penMatchTransactionNames.getNickname4().length() < 1) {
			penMatchTransactionNames.setNickname4(nextNickname);
		}
	}

	/**
	 * Converts PEN Demog record to a PEN Master record
	 * 
	 * @param entity
	 * @return
	 */
	public static PenMasterRecord convertPenDemogToPenMasterRecord(PenDemographicsEntity entity) {
		PenMasterRecord masterRecord = new PenMasterRecord();

		masterRecord.setStudentNumber(entity.getStudNo());
		masterRecord.setDob(entity.getStudBirth());
		masterRecord.setSurname(entity.getStudSurname());
		masterRecord.setGiven(entity.getStudGiven());
		masterRecord.setMiddle(entity.getStudMiddle());
		masterRecord.setUsualSurname(entity.getUsualSurname());
		masterRecord.setUsualGivenName(entity.getUsualGiven());
		masterRecord.setUsualMiddleName(entity.getUsualMiddle());
		masterRecord.setPostal(entity.getPostalCode());
		masterRecord.setSex(entity.getStudSex());
		masterRecord.setGrade(entity.getGrade());
		masterRecord.setStatus(entity.getStudStatus());
		masterRecord.setMincode(entity.getMincode());
		masterRecord.setLocalId(entity.getLocalID());

		return masterRecord;
	}

	/**
	 * Check that the core data is there for a pen master add
	 * 
	 * @param student
	 */
	public static void checkForCoreData(PenMatchStudent student, PenMatchSession session) {
		if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
			session.setPenStatus(PenStatus.G0.getValue());
		}
	}

	/**
	 * Strip off leading zeros , leading blanks and trailing blanks from the
	 * PEN_MASTER stud_local_id. Put result in MAST_PEN_ALT_LOCAL_ID
	 */
	public static void normalizeLocalIDsFromMaster(PenMasterRecord master) {
		master.setAlternateLocalId("MMM");
		if (master.getLocalId() != null) {
			master.setAlternateLocalId(StringUtils.stripStart(master.getLocalId(), "0").replaceAll(" ", ""));
		}
	}

	/**
	 * This function stores all names in an object It includes some split logic for
	 * given/middle names
	 * 
	 * @param master
	 */
	public static PenMatchNames storeNamesFromMaster(PenMasterRecord master) {
		String given = master.getGiven();
		String usualGiven = master.getUsualGivenName();

		PenMatchNames penMatchMasterNames;
		penMatchMasterNames = new PenMatchNames();

		penMatchMasterNames.setLegalGiven(storeNameIfNotNull(given));
		penMatchMasterNames.setLegalMiddle(storeNameIfNotNull(master.getMiddle()));
		penMatchMasterNames.setUsualGiven(storeNameIfNotNull(usualGiven));
		penMatchMasterNames.setUsualMiddle(storeNameIfNotNull(master.getUsualMiddleName()));

		if (given != null) {
			int spaceIndex = StringUtils.indexOf(given, " ");
			if (spaceIndex != -1) {
				penMatchMasterNames.setAlternateLegalGiven(given.substring(0, spaceIndex));
				penMatchMasterNames.setAlternateLegalMiddle(given.substring(spaceIndex).trim());
			}
			int dashIndex = StringUtils.indexOf(given, "-");
			if (dashIndex != -1) {
				penMatchMasterNames.setAlternateLegalGiven(given.substring(0, dashIndex));
				penMatchMasterNames.setAlternateLegalMiddle(given.substring(dashIndex).trim());
			}
		}

		if (usualGiven != null) {
			int spaceIndex = StringUtils.indexOf(usualGiven, " ");
			if (spaceIndex != -1) {
				penMatchMasterNames.setAlternateUsualGiven(usualGiven.substring(0, spaceIndex));
				penMatchMasterNames.setAlternateUsualMiddle(usualGiven.substring(spaceIndex).trim());
			}
			int dashIndex = StringUtils.indexOf(usualGiven, "-");
			if (dashIndex != -1) {
				penMatchMasterNames.setAlternateUsualGiven(usualGiven.substring(0, dashIndex));
				penMatchMasterNames.setAlternateUsualMiddle(usualGiven.substring(dashIndex).trim());
			}
		}
		return penMatchMasterNames;
	}

	/**
	 * Small utility method for storing names to keep things clean
	 * 
	 * @return
	 */
	private static String storeNameIfNotNull(String name) {
		if (name != null && !name.isEmpty()) {
			return name.trim();
		}
		return null;
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
	public static boolean penCheckDigit(String pen) {
		if (pen == null || pen.length() != 9 || !pen.matches("-?\\d+(\\.\\d+)?")) {
			return false;
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

		if ((finalSum % 10 == 0 && penCheckDigit.equals("0")) || ((10 - finalSum % 10) == Integer.parseInt(penCheckDigit))) {
			return true;
		} else {
			return false;
		}
	}

}
