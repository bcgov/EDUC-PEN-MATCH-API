package ca.bc.gov.educ.api.penmatch.util;

import org.apache.commons.codec.language.Soundex;

import ca.bc.gov.educ.api.penmatch.struct.GivenNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.LocalIDMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.MiddleNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.SurnameMatchResult;

public class ScoringUtils {

//	public static final String SOUNDEX_CHARACTERS = StringUtils.repeat(" ", 65) + "01230120022455012623010202"
//			+ StringUtils.repeat(" ", 6) + "01230120022455012623010202" + StringUtils.repeat(" ", 5);

	/**
	 * Calculate points for Birthday match
	 */
	public static Integer matchBirthday(PenMatchStudent student, PenMasterRecord master) {
		Integer birthdayPoints = 0;
		Integer birthdayMatches = 0;
		String dob = student.getDob();

		String masterDob = master.getDob();
		if (dob == null) {
			return null;
		}

		for (int i = 3; i < 8; i++) {
			if (dob.substring(i, i + 1).equals(masterDob.substring(i, i + 1))) {
				birthdayMatches = birthdayMatches + 1;
			}
		}

		// Check for full match
		if (dob.trim().equals(masterDob.trim())) {
			birthdayPoints = 20;
		} else {
			// Same year, month/day flip
			if (dob.substring(0, 4).equals(masterDob.substring(0, 4))
					&& dob.substring(4, 6).equals(masterDob.substring(6, 8))
					&& dob.substring(6, 8).equals(masterDob.substring(4, 6))) {
				birthdayPoints = 15;
			} else {
				// 5 out of 6 right most digits
				if (birthdayMatches >= 5) {
					birthdayPoints = 15;
				} else {
					// Same year and month
					if (dob.substring(0, 6).equals(masterDob.substring(0, 6))) {
						birthdayPoints = 10;
					} else {
						// Same year and day
						if (dob.substring(0, 4).equals(masterDob.substring(0, 4))
								&& dob.substring(6, 8).equals(masterDob.substring(6, 8))) {
							birthdayPoints = 10;
						} else {
							// Same month and day
							if (dob.substring(4, 8).equals(masterDob.substring(4, 8))) {
								birthdayPoints = 5;
							} else {
								// Same year
								if (dob.substring(0, 4).equals(masterDob.substring(0, 4))) {
									birthdayPoints = 5;
								}
							}
						}
					}
				}
			}
		}

		return birthdayPoints;
	}

	/**
	 * Calculate points for Local ID/School code combination - Some schools misuse
	 * the local ID field with a bogus 1 character local ID unfortunately this will
	 * jeopardize the checking for legit 1 character local IDs
	 */
	public static LocalIDMatchResult matchLocalID(PenMatchStudent student, PenMasterRecord master, PenMatchSession session) {
		LocalIDMatchResult matchResult = new LocalIDMatchResult();
		Integer localIDPoints = 0;
		String mincode = student.getMincode();
		String localID = student.getLocalID();
		String masterMincode = master.getMincode();
		String masterLocalID = master.getLocalId();

		if (mincode != null && masterMincode != null && mincode.equals(masterMincode)
				&& ((localID != null && masterLocalID != null && localID.equals(masterLocalID))
						|| (session.getAlternateLocalID() != null
								&& session.getAlternateLocalID().equals(master.getAlternateLocalId())))
				&& (localID != null && localID.trim().length() > 1)) {
			localIDPoints = 20;
		}

		// Same School
		if (localIDPoints == 0) {
			if (mincode != null && masterMincode != null && mincode.equals(masterMincode)) {
				localIDPoints = 10;
			}
		}

		// Same district
		if (localIDPoints == 0) {
			if (mincode != null && masterMincode != null
					&& mincode.substring(0, 3).equals(masterMincode.substring(0, 3))
					&& !mincode.substring(0, 3).equals("102")) {
				localIDPoints = 5;
			}
		}

		// Prepare to negate any local_id_points if the local ids actually conflict
		if (localIDPoints > 0 && mincode != null && masterMincode != null && mincode.equals(masterMincode)) {
			if ((localID == null && masterLocalID != null) || (localID != null && masterLocalID == null)
					|| (localID != null && masterLocalID != null && !localID.equals(masterLocalID))) {
				if ((session.getAlternateLocalID() != null && master.getAlternateLocalId() != null
						&& !session.getAlternateLocalID().equals(master.getAlternateLocalId()))
						|| (session.getAlternateLocalID() == null && master.getAlternateLocalId() == null)) {
					matchResult.setIdDemerits(localIDPoints);
				}
			}
		}

		matchResult.setLocalIDPoints(localIDPoints);

		return matchResult;
	}

	/**
	 * Calculate points for address match
	 */
	public static Integer matchAddress(PenMatchStudent student, PenMasterRecord master) {
		Integer addressPoints = 0;
		String postal = student.getPostal();
		String masterPostal = master.getPostal();

		if (postal != null && masterPostal != null && postal.equals(masterPostal)
				&& !masterPostal.substring(0, 2).equals("V0")) {
			addressPoints = 10;
		} else if (postal != null && masterPostal != null && postal.equals(masterPostal)
				&& masterPostal.substring(0, 2).equals("V0")) {
			addressPoints = 1;
		}

		return addressPoints;
	}

	/**
	 * Calculate points for surname match
	 */
	public static SurnameMatchResult matchSurname(PenMatchStudent student, PenMasterRecord master) {
		Integer surnamePoints = 0;
		boolean legalSurnameUsed = false;
		String masterLegalSurnameNoBlanks = null;
		String masterUsualSurnameNoBlanks = null;
		String studentSurnameNoBlanks = null;
		String usualSurnameNoBlanks = null;

		if (master.getSurname() != null) {
			masterLegalSurnameNoBlanks = master.getSurname().replaceAll(" ", "");
		}
		if (master.getUsualSurname() != null) {
			masterUsualSurnameNoBlanks = master.getUsualSurname().replaceAll(" ", "");
		}
		if (student.getSurname() != null) {
			studentSurnameNoBlanks = student.getSurname().replaceAll(" ", "");
		}
		if (student.getUsualSurname() != null) {
			usualSurnameNoBlanks = student.getUsualSurname().replaceAll(" ", "");
		}

		if (studentSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null
				&& studentSurnameNoBlanks.equals(masterLegalSurnameNoBlanks)) {
			// Verify if legal surname matches master legal surname
			surnamePoints = 20;
			legalSurnameUsed = true;
		} else if (usualSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null
				&& usualSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) {
			// Verify is usual surname matches master usual surname
			surnamePoints = 20;
		} else if (studentSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null
				&& studentSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) {
			// Verify if legal surname matches master usual surname
			surnamePoints = 20;
			legalSurnameUsed = true;
		} else if (usualSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null
				&& usualSurnameNoBlanks.equals(masterLegalSurnameNoBlanks)) {
			// Verify if usual surname matches master legal surname
			surnamePoints = 20;
		} else if (studentSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null
				&& studentSurnameNoBlanks.length() >= 4 && masterLegalSurnameNoBlanks.length() >= 4
				&& studentSurnameNoBlanks.substring(0, 4).equals(masterLegalSurnameNoBlanks.substring(0, 4))) {
			// Do a 4 character match with legal surname and master legal surname
			surnamePoints = 10;
		} else if (usualSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null
				&& usualSurnameNoBlanks.length() >= 4 && masterUsualSurnameNoBlanks.length() >= 4
				&& usualSurnameNoBlanks.substring(0, 4).equals(masterUsualSurnameNoBlanks.substring(0, 4))) {
			// Do a 4 character match with usual surname and master usual surname
			surnamePoints = 10;
		} else if (studentSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null
				&& studentSurnameNoBlanks.length() >= 4 && masterUsualSurnameNoBlanks.length() >= 4
				&& studentSurnameNoBlanks.substring(0, 4).equals(masterUsualSurnameNoBlanks.substring(0, 4))) {
			// Do a 4 character match with legal surname and master usual surname
			surnamePoints = 10;
		} else if (usualSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null
				&& usualSurnameNoBlanks.length() >= 4 && masterLegalSurnameNoBlanks.length() >= 4
				&& usualSurnameNoBlanks.substring(0, 4).equals(masterLegalSurnameNoBlanks.substring(0, 4))) {
			// Do a 4 character match with usual surname and master legal surname
			surnamePoints = 10;
		} else if (surnamePoints == 0) {
			String masterSoundexLegalSurname = runSoundex(masterLegalSurnameNoBlanks);
			String masterSoundexUsualSurname = runSoundex(masterUsualSurnameNoBlanks);

			String soundexLegalSurname = runSoundex(studentSurnameNoBlanks);
			String soundexUsualSurname = runSoundex(usualSurnameNoBlanks);

			if (soundexLegalSurname != null && soundexLegalSurname.length() > 0 && masterSoundexLegalSurname != null
					&& !soundexLegalSurname.substring(0, 1).equals(" ")
					&& soundexLegalSurname.equals(masterSoundexLegalSurname)) {
				// Check if the legal surname soundex matches the master legal surname soundex
				surnamePoints = 10;
			} else if (soundexUsualSurname != null && soundexUsualSurname.length() > 0
					&& masterSoundexLegalSurname != null && !soundexUsualSurname.substring(0, 1).equals(" ")
					&& soundexUsualSurname.equals(masterSoundexLegalSurname)) {
				// Check if the usual surname soundex matches the master legal surname soundex
				surnamePoints = 10;
			} else if (soundexLegalSurname != null && soundexLegalSurname.length() > 0
					&& masterSoundexUsualSurname != null && !soundexLegalSurname.substring(0, 1).equals(" ")
					&& soundexLegalSurname.equals(masterSoundexUsualSurname)) {
				// Check if the legal surname soundex matches the master usual surname soundex
				surnamePoints = 10;
			} else if (soundexUsualSurname != null && soundexUsualSurname.length() > 0
					&& masterSoundexUsualSurname != null && !soundexUsualSurname.substring(0, 1).equals(" ")
					&& soundexUsualSurname.equals(masterSoundexUsualSurname)) {
				// Check if the usual surname soundex matches the master usual surname soundex
				surnamePoints = 10;
			}
		}

		SurnameMatchResult result = new SurnameMatchResult();
		result.setLegalSurnameUsed(legalSurnameUsed);
		result.setSurnamePoints(surnamePoints);
		return result;
	}

	/**
	 * Calculate points for given name match
	 */
	public static GivenNameMatchResult matchGivenName(PenMatchNames penMatchTransactionNames,
			PenMatchNames penMatchMasterNames) {
		Integer givenNamePoints = 0;
		boolean givenFlip = false;

		// Match given to given - use 10 characters

		String legalGiven = penMatchTransactionNames.getLegalGiven();
		String usualGiven = penMatchTransactionNames.getUsualGiven();
		String alternateLegalGiven = penMatchTransactionNames.getAlternateLegalGiven();
		String alternateUsualGiven = penMatchTransactionNames.getAlternateUsualGiven();

		String nickname1 = penMatchTransactionNames.getNickname1();
		String nickname2 = penMatchTransactionNames.getNickname2();
		String nickname3 = penMatchTransactionNames.getNickname3();
		String nickname4 = penMatchTransactionNames.getNickname4();

		if ((hasGivenNameFullCharMatch(legalGiven, penMatchMasterNames))
				|| (hasGivenNameFullCharMatch(usualGiven, penMatchMasterNames))
				|| (hasGivenNameFullCharMatch(alternateLegalGiven, penMatchMasterNames))
				|| (hasGivenNameFullCharMatch(alternateUsualGiven, penMatchMasterNames))) {
			// 10 Character match
			givenNamePoints = 20;
		} else if ((hasGivenNameSubsetCharMatch(legalGiven, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(usualGiven, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateLegalGiven, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateUsualGiven, 10, penMatchMasterNames))) {
			// 10 Character match
			givenNamePoints = 20;
		} else if ((hasGivenNameSubsetMatch(legalGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetMatch(usualGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetMatch(alternateLegalGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetMatch(alternateUsualGiven, penMatchMasterNames))) {
			// Has a subset match
			givenNamePoints = 15;
		} else if ((hasGivenNameSubsetCharMatch(legalGiven, 4, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(usualGiven, 4, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateLegalGiven, 4, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateUsualGiven, 4, penMatchMasterNames))) {
			// 4 Character Match
			givenNamePoints = 15;
		} else if ((hasGivenNameSubsetCharMatch(nickname1, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(nickname2, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(nickname3, 10, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(nickname4, 10, penMatchMasterNames))) {
			// No 4 character matches found , try nicknames
			givenNamePoints = 10;
		} else if ((hasGivenNameSubsetCharMatch(legalGiven, 1, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(usualGiven, 1, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateLegalGiven, 1, penMatchMasterNames))
				|| (hasGivenNameSubsetCharMatch(alternateUsualGiven, 1, penMatchMasterNames))) {
			// 1 Character Match
			givenNamePoints = 5;
		} else if ((hasGivenNameSubsetToMiddleNameMatch(legalGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetToMiddleNameMatch(usualGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetToMiddleNameMatch(alternateLegalGiven, penMatchMasterNames))
				|| (hasGivenNameSubsetToMiddleNameMatch(alternateUsualGiven, penMatchMasterNames))) {
			// Check Given to Middle if no matches above (only try 4 characters)
			givenNamePoints = 10;
			givenFlip = true;
		}

		GivenNameMatchResult result = new GivenNameMatchResult();
		result.setGivenNamePoints(givenNamePoints);
		result.setGivenNameFlip(givenFlip);
		return result;
	}

	/**
	 * Calculate points for middle name match
	 */
	public static MiddleNameMatchResult matchMiddleName(PenMatchNames penMatchTransactionNames,
			PenMatchNames penMatchMasterNames) {
		Integer middleNamePoints = 0;
		boolean middleFlip = false;

		String legalMiddle = penMatchTransactionNames.getLegalMiddle();
		String usualMiddle = penMatchTransactionNames.getUsualMiddle();
		String alternateLegalMiddle = penMatchTransactionNames.getAlternateLegalMiddle();
		String alternateUsualMiddle = penMatchTransactionNames.getAlternateUsualMiddle();

		if ((hasMiddleNameFullCharMatch(legalMiddle, penMatchMasterNames))
				|| (hasMiddleNameFullCharMatch(usualMiddle, penMatchMasterNames))
				|| (hasMiddleNameFullCharMatch(alternateLegalMiddle, penMatchMasterNames))
				|| (hasMiddleNameFullCharMatch(alternateUsualMiddle, penMatchMasterNames))) {
			// Full Match
			middleNamePoints = 20;
		} else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 10, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(usualMiddle, 10, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 10, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 10, penMatchMasterNames))) {
			// 10 Character match
			middleNamePoints = 20;
		} else if ((hasMiddleNameSubsetMatch(legalMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetMatch(usualMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetMatch(alternateLegalMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetMatch(alternateUsualMiddle, penMatchMasterNames))) {
			// Has a subset match
			middleNamePoints = 15;
		} else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 4, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(usualMiddle, 4, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 4, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 4, penMatchMasterNames))) {
			// 4 Character Match
			middleNamePoints = 15;
		} else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 1, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(usualMiddle, 1, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 1, penMatchMasterNames))
				|| (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 1, penMatchMasterNames))) {
			// 1 Character Match
			middleNamePoints = 5;
		} else if ((hasMiddleNameSubsetToGivenNameMatch(legalMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetToGivenNameMatch(usualMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetToGivenNameMatch(alternateLegalMiddle, penMatchMasterNames))
				|| (hasMiddleNameSubsetToGivenNameMatch(alternateUsualMiddle, penMatchMasterNames))) {
			// Check middle to given if no matches above (only try 4 characters)
			middleNamePoints = 10;
			middleFlip = true;
		}

		MiddleNameMatchResult result = new MiddleNameMatchResult();
		result.setMiddleNamePoints(middleNamePoints);
		result.setMiddleNameFlip(middleFlip);
		return result;
	}

	/**
	 * Soundex calculation
	 * 
	 * @param inputString
	 * @return
	 */
	private static String runSoundex(String inputString) {

		String previousCharRaw = null;
		Integer previousCharSoundex = null;
		String currentCharRaw = null;
		Integer currentCharSoundex = null;
		String soundexString = null;
		String tempString = null;

		if (inputString != null && inputString.length() >= 1) {
			Soundex soundex = new Soundex();
			tempString = soundex.soundex(inputString);
			// tempString = StrUtil.xlate(inputString, inputString, SOUNDEX_CHARACTERS);
			soundexString = inputString.substring(0, 1);
			previousCharRaw = inputString.substring(0, 1);
			previousCharSoundex = -1;

			for (int i = 1; i < tempString.length(); i++) {
				currentCharRaw = inputString.substring(i-1, i);
				currentCharSoundex = -1;
				
				try {
					currentCharSoundex = Integer.valueOf(tempString.substring(i-1, i));
				} catch (NumberFormatException e) {
					//This is OK
				}

				if (currentCharSoundex >= 1 && currentCharSoundex <= 7) {
					// If the second "soundexable" character is not the same as the first raw
					// character then append the soundex value of this character to the soundex
					// string. If this is the third or greater soundexable value, then if the
					// soundex
					// value of the character is not equal to the soundex value of the previous
					// character, then append that soundex value to the soundex string.
					if (i == 1) {
						if (currentCharRaw != previousCharRaw) {
							soundexString = soundexString + currentCharSoundex;
							previousCharSoundex = currentCharSoundex;
						}
					} else if (currentCharSoundex != previousCharSoundex) {
						soundexString = soundexString + currentCharSoundex;
						previousCharSoundex = currentCharSoundex;
					}
				}
			}

			soundexString = (soundexString + "00000000").substring(0, 8);
		} else {
			// soundexString = "10000000";
			return null;
		}

		return soundexString;
	}

	/**
	 * Utility function to check for subset given name matches
	 * 
	 * @param givenName
	 * @return
	 */
	public static boolean hasGivenNameSubsetMatch(String givenName, PenMatchNames penMatchMasterNames) {
		if (givenName != null && givenName.length() >= 1) {
			if ((penMatchMasterNames.getLegalGiven() != null && (penMatchMasterNames.getLegalGiven().contains(givenName)
					|| givenName.contains(penMatchMasterNames.getLegalGiven())))
					|| (penMatchMasterNames.getUsualGiven() != null
							&& (penMatchMasterNames.getUsualGiven().contains(givenName)
									|| givenName.contains(penMatchMasterNames.getUsualGiven())))
					|| (penMatchMasterNames.getAlternateLegalGiven() != null
							&& (penMatchMasterNames.getAlternateLegalGiven().contains(givenName)
									|| givenName.contains(penMatchMasterNames.getAlternateLegalGiven())))
					|| (penMatchMasterNames.getAlternateUsualGiven() != null
							&& (penMatchMasterNames.getAlternateUsualGiven().contains(givenName)
									|| givenName.contains(penMatchMasterNames.getAlternateUsualGiven())))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function for subset match
	 * 
	 * @param givenName
	 * @param numOfChars
	 * @return
	 */
	public static boolean hasGivenNameFullCharMatch(String givenName, PenMatchNames penMatchMasterNames) {
		if (givenName != null) {
			if ((penMatchMasterNames.getLegalGiven() != null && penMatchMasterNames.getLegalGiven().equals(givenName))
					|| (penMatchMasterNames.getUsualGiven() != null
							&& penMatchMasterNames.getUsualGiven().equals(givenName))
					|| (penMatchMasterNames.getAlternateLegalGiven() != null
							&& penMatchMasterNames.getAlternateLegalGiven().equals(givenName))
					|| (penMatchMasterNames.getAlternateUsualGiven() != null
							&& penMatchMasterNames.getAlternateUsualGiven().equals(givenName))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function for subset match
	 * 
	 * @param givenName
	 * @param numOfChars
	 * @return
	 */
	public static boolean hasGivenNameSubsetCharMatch(String givenName, int numOfChars, PenMatchNames penMatchMasterNames) {
		if (givenName != null && givenName.length() >= numOfChars) {
			if ((penMatchMasterNames.getLegalGiven() != null
					&& penMatchMasterNames.getLegalGiven().length() >= numOfChars
					&& penMatchMasterNames.getLegalGiven().substring(0, numOfChars)
							.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getUsualGiven() != null
							&& penMatchMasterNames.getUsualGiven().length() >= numOfChars
							&& penMatchMasterNames.getUsualGiven().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateLegalGiven() != null
							&& penMatchMasterNames.getAlternateLegalGiven().length() >= numOfChars
							&& penMatchMasterNames.getAlternateLegalGiven().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateUsualGiven() != null
							&& penMatchMasterNames.getAlternateUsualGiven().length() >= numOfChars
							&& penMatchMasterNames.getAlternateUsualGiven().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function to check for subset given name matches to given names
	 * 
	 * @param givenName
	 * @return
	 */
	public static boolean hasGivenNameSubsetToMiddleNameMatch(String givenName, PenMatchNames penMatchMasterNames) {
		int numOfChars = 4;
		if (givenName != null && givenName.length() >= numOfChars) {
			if ((penMatchMasterNames.getLegalMiddle() != null
					&& penMatchMasterNames.getLegalMiddle().length() >= numOfChars
					&& penMatchMasterNames.getLegalMiddle().substring(0, numOfChars)
							.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getUsualMiddle() != null
							&& penMatchMasterNames.getUsualMiddle().length() >= numOfChars
							&& penMatchMasterNames.getUsualMiddle().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateLegalMiddle() != null
							&& penMatchMasterNames.getAlternateLegalMiddle().length() >= numOfChars
							&& penMatchMasterNames.getAlternateLegalMiddle().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateUsualMiddle() != null
							&& penMatchMasterNames.getAlternateUsualMiddle().length() >= numOfChars
							&& penMatchMasterNames.getAlternateUsualMiddle().substring(0, numOfChars)
									.equals(givenName.substring(0, numOfChars)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function to check for subset middle name matches
	 * 
	 * @param middleName
	 * @return
	 */
	public static boolean hasMiddleNameSubsetMatch(String middleName, PenMatchNames penMatchMasterNames) {
		if (middleName != null && middleName.length() >= 1) {
			if ((penMatchMasterNames.getLegalMiddle() != null
					&& (penMatchMasterNames.getLegalMiddle().contains(middleName)
							|| middleName.contains(penMatchMasterNames.getLegalMiddle())))
					|| (penMatchMasterNames.getUsualMiddle() != null
							&& (penMatchMasterNames.getUsualMiddle().contains(middleName)
									|| middleName.contains(penMatchMasterNames.getUsualMiddle())))
					|| (penMatchMasterNames.getAlternateLegalMiddle() != null
							&& (penMatchMasterNames.getAlternateLegalMiddle().contains(middleName)
									|| middleName.contains(penMatchMasterNames.getAlternateLegalMiddle())))
					|| (penMatchMasterNames.getAlternateUsualMiddle() != null
							&& (penMatchMasterNames.getAlternateUsualMiddle().contains(middleName)
									|| middleName.contains(penMatchMasterNames.getAlternateUsualMiddle())))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function for subset match
	 * 
	 * @param middleName
	 * @param numOfChars
	 * @return
	 */
	public static boolean hasMiddleNameFullCharMatch(String middleName, PenMatchNames penMatchMasterNames) {
		if (middleName != null) {
			if ((penMatchMasterNames.getLegalMiddle() != null
					&& penMatchMasterNames.getLegalMiddle().equals(middleName))
					|| (penMatchMasterNames.getUsualMiddle() != null
							&& penMatchMasterNames.getUsualMiddle().equals(middleName))
					|| (penMatchMasterNames.getAlternateLegalMiddle() != null
							&& penMatchMasterNames.getAlternateLegalMiddle().equals(middleName))
					|| (penMatchMasterNames.getAlternateUsualMiddle() != null
							&& penMatchMasterNames.getAlternateUsualMiddle().equals(middleName))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function for subset match
	 * 
	 * @param middleName
	 * @param numOfChars
	 * @return
	 */
	public static boolean hasMiddleNameSubsetCharMatch(String middleName, int numOfChars, PenMatchNames penMatchMasterNames) {
		if (middleName != null && middleName.length() >= numOfChars) {
			if ((penMatchMasterNames.getLegalMiddle() != null
					&& penMatchMasterNames.getLegalMiddle().length() >= numOfChars
					&& penMatchMasterNames.getLegalMiddle().substring(0, numOfChars)
							.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getUsualMiddle() != null
							&& penMatchMasterNames.getUsualMiddle().length() >= numOfChars
							&& penMatchMasterNames.getUsualMiddle().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateLegalMiddle() != null
							&& penMatchMasterNames.getAlternateLegalMiddle().length() >= numOfChars
							&& penMatchMasterNames.getAlternateLegalMiddle().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateUsualMiddle() != null
							&& penMatchMasterNames.getAlternateUsualMiddle().length() >= numOfChars
							&& penMatchMasterNames.getAlternateUsualMiddle().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility function to check for subset middle name matches to given names
	 * 
	 * @param middleName
	 * @return
	 */
	public static boolean hasMiddleNameSubsetToGivenNameMatch(String middleName, PenMatchNames penMatchMasterNames) {
		int numOfChars = 4;
		if (middleName != null && middleName.length() >= numOfChars) {
			if ((penMatchMasterNames.getLegalGiven() != null
					&& penMatchMasterNames.getLegalGiven().length() >= numOfChars
					&& penMatchMasterNames.getLegalGiven().substring(0, numOfChars)
							.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getUsualGiven() != null
							&& penMatchMasterNames.getUsualGiven().length() >= numOfChars
							&& penMatchMasterNames.getUsualGiven().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateLegalGiven() != null
							&& penMatchMasterNames.getAlternateLegalGiven().length() >= numOfChars
							&& penMatchMasterNames.getAlternateLegalGiven().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))
					|| (penMatchMasterNames.getAlternateUsualGiven() != null
							&& penMatchMasterNames.getAlternateUsualGiven().length() >= numOfChars
							&& penMatchMasterNames.getAlternateUsualGiven().substring(0, numOfChars)
									.equals(middleName.substring(0, numOfChars)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate points for Sex match
	 */
	public static Integer matchSex(PenMatchStudent student, PenMasterRecord master) {
		Integer sexPoints = 0;
		if (student.getSex() != null && student.getSex() == master.getSex()) {
			sexPoints = 5;
		}
		return sexPoints;
	}
}
