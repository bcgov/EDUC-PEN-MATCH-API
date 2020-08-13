package ca.bc.gov.educ.api.penmatch.service;

import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.compare.PenMatchComparator;
import ca.bc.gov.educ.api.penmatch.enumeration.PenAlgorithm;
import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.CheckForMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.GivenNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.LocalIDMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.MiddleNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenConfirmationResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.SurnameMatchResult;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import ca.bc.gov.educ.api.penmatch.util.ScoringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PenMatchService {

	public static final String CHECK_DIGIT_ERROR_CODE_000 = "000";
	public static final String CHECK_DIGIT_ERROR_CODE_001 = "001";
	public static final Integer VERY_FREQUENT = 500;
	public static final Integer NOT_VERY_FREQUENT = 50;
	public static final Integer VERY_RARE = 5;

	@Getter(AccessLevel.PRIVATE)
	private final PenMatchLookupManager lookupManager;

	@Autowired
	public PenMatchService(final PenMatchLookupManager lookupManager) {
		this.lookupManager = lookupManager;
	}

	/**
	 * This is the main method to match a student
	 * 
	 * @param student
	 * @return
	 */
	public PenMatchSession matchStudent(PenMatchStudent student) {
		log.debug("Received student payload :: {}", student);

		PenMatchSession session = initialize(student);

		PenConfirmationResult confirmationResult = new PenConfirmationResult();
		confirmationResult.setDeceased(false);

		if (student.getPen() != null) {
			boolean validCheckDigit = PenMatchUtils.penCheckDigit(student.getPen());
			if (validCheckDigit) {
				confirmationResult = confirmPEN(student, session);
				if (confirmationResult.getPenConfirmationResultCode() == PenConfirmationResult.PEN_CONFIRMED) {
					if (confirmationResult.getMergedPEN() == null) {
						session.setPenStatus(PenStatus.AA.getValue());
						session.setStudentNumber(confirmationResult.getMasterRecord().getStudentNumber().trim());
					} else {
						session.setPenStatus(PenStatus.B1.getValue());
						session.setStudentNumber(confirmationResult.getMergedPEN());
						session.setPen1(confirmationResult.getMergedPEN());
						session.setNumberOfMatches(1);
					}
				} else if (confirmationResult.getPenConfirmationResultCode() == PenConfirmationResult.PEN_ON_FILE) {
					session.setPenStatus(PenStatus.B.getValue());
					if (confirmationResult.getMasterRecord().getStudentNumber() != null) {
						findMatchesOnPenDemog(student, true, session, confirmationResult.getLocalStudentNumber());
					}
				} else {
					session.setPenStatus(PenStatus.C.getValue());
					findMatchesOnPenDemog(student, false, session, null);
				}

			} else {
				session.setPenStatus(PenStatus.C.getValue());
				findMatchesOnPenDemog(student, false, session, null);
			}
		} else {
			session.setPenStatus(PenStatus.D.getValue());
			findMatchesOnPenDemog(student, false, session, null);
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
		if ((session.getPenStatus() == PenStatus.C0.getValue() || session.getPenStatus() == PenStatus.D0.getValue()) && (student.getUpdateCode() != null && (student.getUpdateCode().equals("Y") || student.getUpdateCode().equals("R")))) {
			PenMatchUtils.checkForCoreData(student, session);
		}

		if (session.getPenStatus() == PenStatus.AA.getValue() || session.getPenStatus() == PenStatus.B1.getValue() || session.getPenStatus() == PenStatus.C1.getValue() || session.getPenStatus() == PenStatus.D1.getValue()) {
			PenMasterRecord masterRecord = lookupManager.lookupStudentByPEN(student.getPen());
			if (masterRecord != null && !masterRecord.getDob().equals(student.getDob())) {
				session.setPenStatusMessage("Birthdays are suspect: " + masterRecord.getDob() + " vs " + student.getDob());
				session.setPenStatus(PenStatus.F1.getValue());
				session.setPen1(student.getPen());
				session.setStudentNumber(null);
			}

			if (masterRecord.getSurname() == student.getSurname() && masterRecord.getGiven() != student.getGivenName() && masterRecord.getDob() == student.getDob() && masterRecord.getMincode() == student.getMincode() && masterRecord.getLocalId() != null && student.getLocalID() != null
					&& !masterRecord.getLocalId().equals(student.getLocalID())) {
				session.setPenStatusMessage("Possible twin: " + masterRecord.getGiven().trim() + " vs " + student.getGivenName().trim());
				session.setPenStatus(PenStatus.F1.getValue());
				session.setPen1(student.getPen());
				session.setStudentNumber(null);
			}
		}

		if (confirmationResult.isDeceased()) {
			session.setPenStatus(PenStatus.C0.getValue());
			session.setStudentNumber(null);
		}

		return session;
	}

	/**
	 * Initialize the student record and variables (will be refactored)
	 * 
	 * @param student
	 * @return
	 */
	private PenMatchSession initialize(PenMatchStudent student) {
		PenMatchSession session = new PenMatchSession();
		session.setPenStatusMessage(null);
		session.setMatchingRecords(new PriorityQueue<PenMatchRecord>(new PenMatchComparator()));

		session.setReallyGoodMatches(0);
		session.setPrettyGoodMatches(0);
		session.setReallyGoodPEN(null);
		session.setNumberOfMatches(0);
		student.setAlternateLocalID("TTT");

		// Strip off leading zeros, leading blanks and trailing blanks
		// from the local_id. Put result in alternateLocalID.
		if (student.getLocalID() != null) {
			student.setAlternateLocalID(StringUtils.stripStart(student.getLocalID(), "0").replaceAll(" ", ""));
		}

		student.setPenMatchTransactionNames(storeNamesFromTransaction(student));

		student.setMinSurnameSearchSize(4);
		student.setMaxSurnameSearchSize(6);

		Integer surnameSize = 0;

		if (student.getSurname() != null) {
			surnameSize = student.getSurname().length();
		} else {
			surnameSize = 0;
		}

		if (surnameSize < student.getMinSurnameSearchSize()) {
			student.setMinSurnameSearchSize(surnameSize);
		} else if (surnameSize < student.getMaxSurnameSearchSize()) {
			student.setMaxSurnameSearchSize(surnameSize);
		}

		// Lookup surname frequency
		// It could generate extra points later if
		// there is a perfect match on surname
		Integer fullSurnameFrequency = 0;
		Integer partialSurnameFrequency = 0;
		String fullStudentSurname = student.getSurname();
		fullSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);

		if (fullSurnameFrequency > VERY_FREQUENT) {
			partialSurnameFrequency = fullSurnameFrequency;
		} else {
			fullStudentSurname = student.getSurname().substring(0, student.getMinSurnameSearchSize());
			partialSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);
		}

		student.setFullSurnameFrequency(fullSurnameFrequency);
		student.setPartialSurnameFrequency(partialSurnameFrequency);

		return session;
	}

	/**
	 * This function stores all names in an object It includes some split logic for
	 * given/middle names
	 * 
	 * @param student
	 */
	private PenMatchNames storeNamesFromTransaction(PenMatchStudent student) {
		String given = student.getGivenName();
		String usualGiven = student.getUsualGivenName();
		PenMatchNames penMatchTransactionNames;

		penMatchTransactionNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven(given);
		penMatchTransactionNames.setLegalMiddle(student.getMiddleName());
		penMatchTransactionNames.setUsualGiven(usualGiven);
		penMatchTransactionNames.setUsualMiddle(student.getUsualMiddleName());

		if (given != null) {
			int spaceIndex = StringUtils.indexOf(given, " ");
			if (spaceIndex != -1) {
				penMatchTransactionNames.setAlternateLegalGiven(given.substring(0, spaceIndex));
				penMatchTransactionNames.setAlternateLegalMiddle(given.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(given, "-");
			if (dashIndex != -1) {
				penMatchTransactionNames.setAlternateLegalGiven(given.substring(0, dashIndex));
				penMatchTransactionNames.setAlternateLegalMiddle(given.substring(dashIndex));
			}
		}

		if (usualGiven != null) {
			int spaceIndex = StringUtils.indexOf(usualGiven, " ");
			if (spaceIndex != -1) {
				penMatchTransactionNames.setAlternateUsualGiven(usualGiven.substring(0, spaceIndex));
				penMatchTransactionNames.setAlternateUsualMiddle(usualGiven.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(usualGiven, "-");
			if (dashIndex != -1) {
				penMatchTransactionNames.setAlternateUsualGiven(usualGiven.substring(0, dashIndex));
				penMatchTransactionNames.setAlternateUsualMiddle(usualGiven.substring(dashIndex));
			}
		}

		lookupManager.lookupNicknames(penMatchTransactionNames, given);
		return penMatchTransactionNames;
	}

	/**
	 * Check for exact match on surname , given name, birthday and gender OR exact
	 * match on school and local ID and one or more of surname, given name or
	 * birthday
	 */
	private CheckForMatchResult simpleCheckForMatch(PenMatchStudent student, PenMasterRecord master, PenMatchSession session) {
		boolean matchFound = false;
		PenAlgorithm algorithmUsed = null;

		if (student.getSurname() != null && student.getSurname().equals(master.getSurname().trim()) && student.getGivenName() != null && student.getGivenName().equals(master.getGiven().trim()) && student.getDob() != null && student.getDob().equals(master.getDob()) && student.getSex() != null
				&& student.getSex().equals(master.getSex())) {
			matchFound = true;
			algorithmUsed = PenAlgorithm.ALG_S1;
		} else if (student.getSurname() != null && student.getSurname().equals(master.getSurname().trim()) && student.getGivenName() != null && student.getGivenName().equals(master.getGiven().trim()) && student.getDob() != null && student.getDob().equals(master.getDob())
				&& student.getLocalID() != null && student.getLocalID().length() > 1) {
			PenMatchUtils.normalizeLocalIDsFromMaster(master);
			if (student.getMincode() != null && student.getMincode().equals(master.getMincode()) && ((student.getLocalID() != null && student.getLocalID().equals(master.getLocalId())) || (student.getAlternateLocalID() != null && student.getAlternateLocalID().equals(master.getAlternateLocalId())))) {
				matchFound = true;
				algorithmUsed = PenAlgorithm.ALG_S2;
			}
		}

		if (matchFound) {
			loadPenMatchHistory();
		}

		CheckForMatchResult result = new CheckForMatchResult();
		result.setMatchFound(matchFound);
		result.setType5F1(false);
		result.setAlgorithmUsed(algorithmUsed);

		return result;
	}

	/**
	 * Confirm that the PEN on transaction is correct.
	 * 
	 * @param student
	 * @return
	 */
	private PenConfirmationResult confirmPEN(PenMatchStudent student, PenMatchSession session) {
		PenConfirmationResult result = new PenConfirmationResult();

		String localStudentNumber = student.getPen();
		result.setDeceased(false);

		PenMasterRecord masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);

		boolean matchFound = false;

		if (masterRecord != null && masterRecord.getStudentNumber().trim().equals(localStudentNumber)) {
			result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
			if (masterRecord.getStatus() != null && masterRecord.getStatus().equals("M") && masterRecord.getTrueNumber() != null) {
				localStudentNumber = masterRecord.getTrueNumber();
				result.setMergedPEN(masterRecord.getTrueNumber());
				masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);
				if (masterRecord != null && masterRecord.getStudentNumber() == localStudentNumber) {
					matchFound = simpleCheckForMatch(student, masterRecord, session).isMatchFound();
					if (masterRecord.getStatus().equals("D")) {
						localStudentNumber = null;
						result.setDeceased(true);
					}
				}
			} else {
				matchFound = simpleCheckForMatch(student, masterRecord, session).isMatchFound();
			}
			if (matchFound) {
				result.setPenConfirmationResultCode(PenConfirmationResult.PEN_CONFIRMED);
			}
		}

		if (matchFound) {
			loadPenMatchHistory();
		}

		result.setLocalStudentNumber(localStudentNumber);
		result.setMasterRecord(masterRecord);

		return result;
	}

	/**
	 * Find all possible students on master who could match the transaction - If the
	 * first four characters of surname are uncommon then only use 4 characters in
	 * lookup. Otherwise use 6 characters , or 5 if surname is only 5 characters
	 * long use the given initial in the lookup unless 1st 4 characters of surname
	 * is quite rare
	 */
	private void findMatchesOnPenDemog(PenMatchStudent student, boolean penFoundOnMaster, PenMatchSession session, String localStudentNumber) {
		boolean useGivenInitial = true;
		boolean type5F1 = false;

		if (student.getPartialSurnameFrequency() <= NOT_VERY_FREQUENT) {
			student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
			useGivenInitial = false;
		} else {
			if (student.getPartialSurnameFrequency() <= VERY_FREQUENT) {
				student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
				student.setPartialStudentGiven(student.getGivenName().substring(0, 1));
			} else {
				student.setPartialStudentSurname(student.getSurname().substring(0, student.getMaxSurnameSearchSize()));
				student.setPartialStudentGiven(student.getGivenName().substring(0, 2));
			}
		}

		List<PenDemographicsEntity> penDemogList;
		if (student.getLocalID() == null) {
			if (useGivenInitial) {
				penDemogList = lookupManager.lookupNoLocalID(student.getDob(), student.getPartialStudentSurname(), student.getPartialStudentGiven());
			} else {
				penDemogList = lookupManager.lookupNoInitNoLocalID(student.getDob(), student.getPartialStudentSurname());
			}
		} else {
			if (useGivenInitial) {
				penDemogList = lookupManager.lookupWithAllParts(student.getDob(), student.getPartialStudentSurname(), student.getPartialStudentGiven(), student.getMincode(), student.getLocalID());
			} else {
				penDemogList = lookupManager.lookupNoInit(student.getDob(), student.getPartialStudentSurname(), student.getMincode(), student.getLocalID());
			}
		}
		performCheckAndMerge(penDemogList, student, session, localStudentNumber);

		// If a PEN was provided, but the demographics didn't match the student
		// on PEN-MASTER with that PEN, then add the student on PEN-MASTER to
		// the list of possible students who match.
		if (session.getPenStatus().equals(PenStatus.B.getValue()) && penFoundOnMaster) {
			session.setReallyGoodMatches(0);
			type5F1 = true;
			mergeNewMatchIntoList(student, localStudentNumber, session, PenAlgorithm.ALG_00, 0);
		}

		// If only one really good match, and no pretty good matches,
		// just send the one PEN back
		if (session.getPenStatus().substring(0, 1).equals(PenStatus.D.getValue()) && session.getReallyGoodMatches() == 1 && session.getPrettyGoodMatches() == 0) {
			session.setPen1(session.getReallyGoodPEN());
			session.setStudentNumber(session.getReallyGoodPEN());
			session.setNumberOfMatches(1);
			session.setPenStatus(PenStatus.D1.getValue());
			return;
		} else {
//			log.debug("List of matching PENs: {}", session.getMatchingPENs());
//			session.setPen1(session.getMatchingPENs()[0]);
//			session.setPen2(session.getMatchingPENs()[1]);
//			session.setPen3(session.getMatchingPENs()[2]);
//			session.setPen4(session.getMatchingPENs()[3]);
//			session.setPen5(session.getMatchingPENs()[4]);
//			session.setPen6(session.getMatchingPENs()[5]);
//			session.setPen7(session.getMatchingPENs()[6]);
//			session.setPen8(session.getMatchingPENs()[7]);
//			session.setPen9(session.getMatchingPENs()[8]);
//			session.setPen10(session.getMatchingPENs()[9]);
//			session.setPen11(session.getMatchingPENs()[10]);
//			session.setPen12(session.getMatchingPENs()[11]);
//			session.setPen13(session.getMatchingPENs()[12]);
//			session.setPen14(session.getMatchingPENs()[13]);
//			session.setPen15(session.getMatchingPENs()[14]);
//			session.setPen16(session.getMatchingPENs()[15]);
//			session.setPen17(session.getMatchingPENs()[16]);
//			session.setPen18(session.getMatchingPENs()[17]);
//			session.setPen19(session.getMatchingPENs()[18]);
//			session.setPen20(session.getMatchingPENs()[19]);
		}

		if (session.getNumberOfMatches() == 0) {
			// No matches found
			session.setPenStatus(session.getPenStatus().trim() + "0");
			session.setStudentNumber(null);
		} else if (session.getNumberOfMatches() == 1) {
			// 1 match only
			if (type5F1) {
				session.setPenStatus(PenStatus.F.getValue());
				session.setStudentNumber(null);
			} else {
				// one solid match, put in t_stud_no
				session.setStudentNumber(session.getMatchingRecords().peek().getMatchingPEN());
			}
			session.setPenStatus(session.getPenStatus().trim() + "1");
		} else {
			session.setPenStatus(session.getPenStatus().trim() + "M");
			// many matches, so they are all considered questionable, even if some are
			// "solid"
			session.setStudentNumber(null);
		}

	}

	/**
	 * Merge new match into the list Assign points for algorithm and score for sort
	 * use
	 */
	private void mergeNewMatchIntoList(PenMatchStudent student, String matchingPEN, PenMatchSession session, PenAlgorithm algorithmUsed, Integer totalPoints) {
		Integer matchingAlgorithmResult;
		Integer matchingScore;

		switch (algorithmUsed) {
		case ALG_S1:
			matchingAlgorithmResult = 100;
			matchingScore = 100;
			break;
		case ALG_S2:
			matchingAlgorithmResult = 110;
			matchingScore = 100;
			break;
		case ALG_SP:
			matchingAlgorithmResult = 190;
			matchingScore = 100;
			break;
		case ALG_00:
			matchingAlgorithmResult = 0;
			matchingScore = 1;
			break;
		case ALG_20:
		case ALG_30:
		case ALG_40:
		case ALG_50:
		case ALG_51:
			matchingAlgorithmResult = Integer.valueOf(algorithmUsed.getValue()) * 10;
			matchingScore = totalPoints;
			break;
		default:
			log.debug("Unconvertable algorithm code: {}", algorithmUsed);
			matchingAlgorithmResult = 9999;
			matchingScore = 0;
			break;
		}

		if (session.getNumberOfMatches() < 20) {
			// Add new slot in the array
			session.setNumberOfMatches(session.getNumberOfMatches() + 1);
			PenMatchRecord record = new PenMatchRecord(matchingAlgorithmResult, matchingScore, matchingPEN);
			session.getMatchingRecords().add(record);
		}

	}

	/**
	 * Check for Matching demographic data on Master
	 * 
	 * @param student
	 * @param master
	 * @param penMatchTransactionNames
	 * @param penMatchMasterNames
	 * @param algorithmUsed
	 * @param fullSurnameFrequency
	 * @return
	 */
	private CheckForMatchResult checkForMatch(PenMatchStudent student, PenMasterRecord master, PenMatchSession session) {
		boolean matchFound = false;
		boolean type5F1 = false;
		boolean type5Match = false;
		PenAlgorithm algorithmUsed = null;

		PenMatchUtils.normalizeLocalIDsFromMaster(master);
		PenMatchNames penMatchMasterNames = PenMatchUtils.storeNamesFromMaster(master);

		Integer totalPoints = 0;
		Integer bonusPoints = 0;
		Integer idDemerits = 0;

		Integer sexPoints = ScoringUtils.matchSex(student, master); // 5 points
		Integer birthdayPoints = ScoringUtils.matchBirthday(student, master); // 5, 10, 15 or 20 points
		SurnameMatchResult surnameMatchResult = ScoringUtils.matchSurname(student, master); // 10 or 20 points
		GivenNameMatchResult givenNameMatchResult = ScoringUtils.matchGivenName(student.getPenMatchTransactionNames(), penMatchMasterNames); // 5, 10,
		// 15 or
		// 20
		// points

		// If a perfect match on legal surname , add 5 points if a very rare surname
		if (surnameMatchResult.getSurnamePoints() >= 20 && student.getFullSurnameFrequency() <= VERY_RARE && surnameMatchResult.isLegalSurnameUsed()) {
			surnameMatchResult.setSurnamePoints(surnameMatchResult.getSurnamePoints() + 5);
		}

		MiddleNameMatchResult middleNameMatchResult = ScoringUtils.matchMiddleName(student.getPenMatchTransactionNames(), penMatchMasterNames); // 5,
		// 10,
		// 15
		// or
		// 20
		// points

		// If given matches middle and middle matches given and there are some
		// other points, there is a good chance that the names have been flipped
		if (givenNameMatchResult.isGivenNameFlip() && middleNameMatchResult.isMiddleNameFlip() && (surnameMatchResult.getSurnamePoints() >= 10 || birthdayPoints >= 15)) {
			givenNameMatchResult.setGivenNamePoints(15);
			middleNameMatchResult.setMiddleNamePoints(15);
		}

		LocalIDMatchResult localIDMatchResult = ScoringUtils.matchLocalID(student, master, session); // 5, 10 or 20
																										// points
		Integer addressPoints = ScoringUtils.matchAddress(student, master); // 1 or 10 points

		// Special search algorithm - just looks for any points in all of
		// the non-blank search fields provided
		if (student.getUpdateCode() != null && student.getUpdateCode().equals("S")) {
			matchFound = true;
			if (student.getSex() != null && sexPoints == 0) {
				matchFound = false;
			}
			if (!(student.getSurname() != null && student.getUsualSurname() != null) && surnameMatchResult.getSurnamePoints() == 0) {
				matchFound = false;
			}
			if (!(student.getGivenName() != null && student.getUsualGivenName() != null) && givenNameMatchResult.getGivenNamePoints() == 0) {
				matchFound = false;
			}
			if (!(student.getMiddleName() != null && student.getUsualMiddleName() != null) && middleNameMatchResult.getMiddleNamePoints() == 0) {
				matchFound = false;
			}
			if (student.getDob() != null && birthdayPoints == 0) {
				matchFound = false;
			}
			if (!(student.getLocalID() != null && student.getMincode() != null) && localIDMatchResult.getLocalIDPoints() == 0) {
				matchFound = false;
			}
			if (student.getPostal() != null && addressPoints == 0) {
				matchFound = false;
			}

			if (matchFound) {
				type5F1 = true;
				type5Match = true;
				algorithmUsed = PenAlgorithm.ALG_SP;
			}
		}

		// Algorithm 1 : used to be Personal Education No. + 40 bonus points
		// Using SIMPLE_MATCH instead

		// Algorithm 2 : Gender + Birthday + Surname + 25 bonus points (not counting
		// school points and address points so twins are weeded out)
		// Bonus points will include same district or same school + localid ,
		// but not same school
		if (!matchFound) {
			if (localIDMatchResult.getLocalIDPoints() == 5 || localIDMatchResult.getLocalIDPoints() == 20) {
				bonusPoints = givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + localIDMatchResult.getLocalIDPoints();
			} else {
				bonusPoints = givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints();
			}

			if (sexPoints >= 5 && birthdayPoints >= 20 && surnameMatchResult.getSurnamePoints() >= 20) {
				if (bonusPoints >= 25) {
					matchFound = true;
					session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
					session.setReallyGoodPEN(master.getStudentNumber());
					totalPoints = sexPoints + birthdayPoints + surnameMatchResult.getSurnamePoints() + bonusPoints;
					algorithmUsed = PenAlgorithm.ALG_20;
				}
			}
		}

		// Algorithm 3 : School/ local ID + Surname + 25 bonus points
		// (65 points total)
		if (!matchFound) {
			if (localIDMatchResult.getLocalIDPoints() >= 20 && surnameMatchResult.getSurnamePoints() >= 20) {
				bonusPoints = sexPoints + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + addressPoints;
				if (bonusPoints >= 25) {
					matchFound = true;
					session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
					session.setReallyGoodPEN(master.getStudentNumber());
					totalPoints = localIDMatchResult.getLocalIDPoints() + surnameMatchResult.getSurnamePoints() + bonusPoints;
					algorithmUsed = PenAlgorithm.ALG_30;
				}
			}
		}

		// Algorithm 4: School/local id + gender + birthdate + 20 bonus points
		// (65 points total)
		if (!matchFound) {
			if (localIDMatchResult.getLocalIDPoints() >= 20 && sexPoints >= 5 && birthdayPoints >= 20) {
				bonusPoints = surnameMatchResult.getSurnamePoints() + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + addressPoints;
				if (bonusPoints >= 20) {
					matchFound = true;
					session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
					session.setReallyGoodPEN(master.getStudentNumber());
					totalPoints = localIDMatchResult.getLocalIDPoints() + sexPoints + birthdayPoints + bonusPoints;
					algorithmUsed = PenAlgorithm.ALG_40;
				}
			}
		}

		// Algorithm 5: Use points for Sex + birthdate + surname + given name +
		// middle name + address + local_id + school >= 55 bonus points
		if (!matchFound) {
			bonusPoints = sexPoints + birthdayPoints + surnameMatchResult.getSurnamePoints() + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + localIDMatchResult.getLocalIDPoints() + addressPoints;
			if (bonusPoints >= idDemerits) {
				bonusPoints = bonusPoints - idDemerits;
			} else {
				bonusPoints = 0;
			}

			if (bonusPoints >= 55 || (bonusPoints >= 40 && localIDMatchResult.getLocalIDPoints() >= 20) || (bonusPoints >= 50 && surnameMatchResult.getSurnamePoints() >= 10 && birthdayPoints >= 15 && givenNameMatchResult.getGivenNamePoints() >= 15) || (bonusPoints >= 50 && birthdayPoints >= 20)
					|| (bonusPoints >= 50 && student.getLocalID().substring(1, 4).equals("ZZZ"))) {
				matchFound = true;
				algorithmUsed = PenAlgorithm.ALG_50;
				totalPoints = bonusPoints;
				if (bonusPoints >= 70) {
					session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
					session.setReallyGoodPEN(master.getStudentNumber());
				} else if (bonusPoints >= 60 || localIDMatchResult.getLocalIDPoints() >= 20) {
					session.setPrettyGoodMatches(session.getPrettyGoodMatches() + 1);
				}
				type5F1 = true;
				type5Match = true;
			}
		}

		// Algorithm 5.1: Use points for Sex + birthdate + surname + given name +
		// middle name + address + local_id + school >= 55 bonus points
		if (!matchFound) {
			if (sexPoints == 5 && birthdayPoints >= 10 && surnameMatchResult.getSurnamePoints() >= 20 && givenNameMatchResult.getGivenNamePoints() >= 10) {
				matchFound = true;
				algorithmUsed = PenAlgorithm.ALG_51;
				totalPoints = 45;

				// Identify a pretty good match - needs to be better than the Questionable Match
				// but not a full 60 points as above
				if (surnameMatchResult.getSurnamePoints() >= 20 && givenNameMatchResult.getGivenNamePoints() >= 15 && birthdayPoints >= 15 && sexPoints == 5) {
					session.setPrettyGoodMatches(session.getPrettyGoodMatches() + 1);
					totalPoints = 55;
				}
				type5F1 = true;
				type5Match = true;
			}
		}

		if (matchFound) {
			loadPenMatchHistory();
		}

		CheckForMatchResult result = new CheckForMatchResult();
		result.setMatchFound(matchFound);
		result.setType5F1(type5F1);
		result.setType5Match(type5Match);
		result.setAlgorithmUsed(algorithmUsed);
		result.setTotalPoints(totalPoints);

		return result;
	}

	/**
	 * Create a log entry for analytical purposes. Not used in our Java
	 * implementation
	 */
	private void loadPenMatchHistory() {
		// Not currently implemented
		// This was a logging function in Basic, we'll likely do something different
	}

	/**
	 * Utility method for checking and merging lookups
	 * 
	 * @param penDemogList
	 * @param student
	 * @param session
	 */
	private void performCheckAndMerge(List<PenDemographicsEntity> penDemogList, PenMatchStudent student, PenMatchSession session, String localStudentNumber) {
		if (penDemogList != null) {
			for (PenDemographicsEntity entity : penDemogList) {
				if (entity.getStudStatus() != null && !entity.getStudStatus().equals(PenStatus.M.getValue()) && !entity.getStudStatus().equals(PenStatus.D.getValue()) && (localStudentNumber == null || !entity.getStudNo().trim().equals(localStudentNumber))) {
					PenMasterRecord masterRecord = PenMatchUtils.convertPenDemogToPenMasterRecord(entity);
					CheckForMatchResult result = checkForMatch(student, masterRecord, session);

					if (result.isMatchFound()) {
						String matchingPEN = null;
						if (result.isType5Match()) {
							matchingPEN = masterRecord.getStudentNumber().trim() + "?";
						} else {
							matchingPEN = masterRecord.getStudentNumber();
						}
						mergeNewMatchIntoList(student, matchingPEN, session, result.getAlgorithmUsed(), result.getTotalPoints());
					}
				}
			}
		}
	}

}
