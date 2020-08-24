package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.compare.PenMatchComparator;
import ca.bc.gov.educ.api.penmatch.enumeration.PenAlgorithm;
import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.*;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import ca.bc.gov.educ.api.penmatch.util.ScoringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.PriorityQueue;

@Service
@Slf4j
public class PenMatchService {

    public static final int VERY_FREQUENT = 500;
    public static final int NOT_VERY_FREQUENT = 50;
    public static final int VERY_RARE = 5;

    @Autowired
    private PenMatchLookupManager lookupManager;

    public PenMatchService(PenMatchLookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    /**
     * This is the main method to match a student
     */
    public PenMatchResult matchStudent(PenMatchStudentDetail student) {
        log.info(" input :: PenMatchStudentDetail={}", PenMatchUtils.getJSONFormatObject(student));
        PenMatchSession session = initialize(student);

        PenConfirmationResult confirmationResult = new PenConfirmationResult();
        confirmationResult.setDeceased(false);

        if (student.getPen() != null) {
            boolean validCheckDigit = PenMatchUtils.penCheckDigit(student.getPen());
            if (validCheckDigit) {
                confirmationResult = confirmPEN(student, session);
                if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_CONFIRMED)) {
                    if (confirmationResult.getMergedPEN() == null) {
                        session.setPenStatus(PenStatus.AA.getValue());
                        session.setStudentNumber(confirmationResult.getMasterRecord().getStudentNumber().trim());
                    } else {
                        session.setPenStatus(PenStatus.B1.getValue());
                        session.setStudentNumber(confirmationResult.getMergedPEN());
                        session.setPen1(confirmationResult.getMergedPEN());
                        session.setNumberOfMatches(1);
                    }
                } else if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
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
        if ((session.getPenStatus().equals(PenStatus.C0.getValue()) || session.getPenStatus().equals(PenStatus.D0.getValue())) && (student.getUpdateCode() != null && (student.getUpdateCode().equals("Y") || student.getUpdateCode().equals("R")))) {
            PenMatchUtils.checkForCoreData(student, session);
        }

        if (session.getPenStatus().equals(PenStatus.AA.getValue()) || session.getPenStatus().equals(PenStatus.B1.getValue()) || session.getPenStatus().equals(PenStatus.C1.getValue()) || session.getPenStatus().equals(PenStatus.D1.getValue())) {
            PenMasterRecord masterRecord = lookupManager.lookupStudentByPEN(student.getPen());
            if (masterRecord != null && !masterRecord.getDob().equals(student.getDob())) {
                session.setPenStatusMessage("Birthdays are suspect: " + masterRecord.getDob() + " vs " + student.getDob());
                session.setPenStatus(PenStatus.F1.getValue());
                session.setPen1(student.getPen());
                session.setStudentNumber(null);
            } else if (masterRecord != null && masterRecord.getSurname().equals(student.getSurname()) && !masterRecord.getGiven().equals(student.getGivenName()) && masterRecord.getDob().equals(student.getDob()) && masterRecord.getMincode().equals(student.getMincode()) && masterRecord.getLocalId() != null
                    && student.getLocalID() != null && !masterRecord.getLocalId().trim().equals(student.getLocalID())) {
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
        PenMatchResult result = new PenMatchResult(session.getMatchingRecords(), session.getStudentNumber(), session.getPenStatus(), session.getPenStatusMessage());
        log.info(" output :: PenMatchResult={}", PenMatchUtils.getJSONFormatObject(result));
        return result;
    }

    /**
     * Initialize the student record and variables (will be refactored)
     */
    private PenMatchSession initialize(PenMatchStudentDetail student) {
        log.info(" input :: PenMatchStudentDetail={}", PenMatchUtils.getJSONFormatObject(student));
        PenMatchSession session = new PenMatchSession();
        session.setPenStatusMessage(null);
        session.setMatchingRecords(new PriorityQueue<>(new PenMatchComparator()));

        PenMatchUtils.upperCaseInputStudent(student);

        session.setReallyGoodMatches(0);
        session.setPrettyGoodMatches(0);
        session.setReallyGoodPEN(null);
        session.setNumberOfMatches(0);
        student.setAlternateLocalID("TTT");

        // Strip off leading zeros, leading blanks and trailing blanks
        // from the local_id. Put result in alternateLocalID.
        if (student.getLocalID() != null) {
            student.setAlternateLocalID(StringUtils.stripStart(student.getLocalID(), "0").replace(" ", ""));
        }

        student.setPenMatchTransactionNames(storeNamesFromTransaction(student));

        student.setMinSurnameSearchSize(4);
        student.setMaxSurnameSearchSize(6);

        int surnameSize = 0;

        if (student.getSurname() != null) {
            surnameSize = student.getSurname().length();
        } else {
            surnameSize = 0;
        }

        if (surnameSize < student.getMinSurnameSearchSize()) {
            student.setMinSurnameSearchSize(surnameSize);
        }

        if (surnameSize < student.getMaxSurnameSearchSize()) {
            student.setMaxSurnameSearchSize(surnameSize);
        }

        // Lookup surname frequency
        // It could generate extra points later if
        // there is a perfect match on surname
        int partialSurnameFrequency = 0;
        String fullStudentSurname = student.getSurname();
        int fullSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);

        if (fullSurnameFrequency > VERY_FREQUENT) {
            partialSurnameFrequency = fullSurnameFrequency;
        } else if (fullStudentSurname != null) {
            fullStudentSurname = student.getSurname().substring(0, student.getMinSurnameSearchSize());
            partialSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);
        }

        student.setFullSurnameFrequency(fullSurnameFrequency);
        student.setPartialSurnameFrequency(partialSurnameFrequency);

        log.info(" output :: PenMatchSession={}", PenMatchUtils.getJSONFormatObject(session));
        return session;
    }

    /**
     * This function stores all names in an object It includes some split logic for
     * given/middle names
     */
    private PenMatchNames storeNamesFromTransaction(PenMatchStudentDetail student) {
        log.info(" input :: PenMatchStudentDetail={}", PenMatchUtils.getJSONFormatObject(student));
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
        log.info(" output :: PenMatchNames={}", PenMatchUtils.getJSONFormatObject(penMatchTransactionNames));
        return penMatchTransactionNames;
    }

    /**
     * Check for exact match on surname , given name, birthday and gender OR exact
     * match on school and local ID and one or more of surname, given name or
     * birthday
     */
    private CheckForMatchResult simpleCheckForMatch(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session) {
        log.info(" input :: PenMatchStudentDetail={} PenMasterRecord={} PenMatchSession={}", PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(master), PenMatchUtils.getJSONFormatObject(session));
        boolean matchFound = false;
        PenAlgorithm algorithmUsed = null;

        if (student.getSurname() != null && student.getSurname().equals(master.getSurname().trim()) && student.getGivenName() != null && student.getGivenName().equals(master.getGiven().trim()) && student.getDob() != null && student.getDob().equals(master.getDob()) && student.getSex() != null
                && student.getSex().equals(master.getSex())) {
            matchFound = true;
            algorithmUsed = PenAlgorithm.ALG_S1;
        } else if (student.getSurname() != null && student.getSurname().equals(master.getSurname().trim()) && student.getGivenName() != null && student.getGivenName().equals(master.getGiven().trim()) && student.getDob() != null && student.getDob().equals(master.getDob())
                && student.getLocalID() != null && student.getLocalID().length() > 1) {
            PenMatchUtils.normalizeLocalIDsFromMaster(master);
            if (student.getMincode() != null && student.getMincode().equals(master.getMincode()) && ((student.getLocalID() != null && master.getLocalId() != null && student.getLocalID().equals(master.getLocalId().trim())) || (student.getAlternateLocalID() != null && master.getAlternateLocalId() != null && student.getAlternateLocalID().equals(master.getAlternateLocalId().trim())))) {
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

        log.info(" output :: CheckForMatchResult={}", PenMatchUtils.getJSONFormatObject(result));
        return result;
    }

    /**
     * Confirm that the PEN on transaction is correct.
     */
    private PenConfirmationResult confirmPEN(PenMatchStudentDetail student, PenMatchSession session) {
        log.info(" input :: PenMatchStudentDetail={} PenMatchSession={}", PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(session));
        PenConfirmationResult result = new PenConfirmationResult();
        result.setPenConfirmationResultCode(PenConfirmationResult.NO_RESULT);

        String localStudentNumber = student.getPen();
        result.setDeceased(false);

        PenMasterRecord masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);

        boolean matchFound = false;

        if (masterRecord != null && masterRecord.getStudentNumber().trim().equals(localStudentNumber)) {
            result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
            if (masterRecord.getStatus() != null && masterRecord.getStatus().equals("M") && masterRecord.getTrueNumber() != null) {
                localStudentNumber = masterRecord.getTrueNumber().trim();
                result.setMergedPEN(masterRecord.getTrueNumber().trim());
                masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);
                if (masterRecord != null && masterRecord.getStudentNumber().trim().equals(localStudentNumber)) {
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

        log.info(" output :: PenConfirmationResult={}", PenMatchUtils.getJSONFormatObject(result));
        return result;
    }

    /**
     * Find all possible students on master who could match the transaction - If the
     * first four characters of surname are uncommon then only use 4 characters in
     * lookup. Otherwise use 6 characters , or 5 if surname is only 5 characters
     * long use the given initial in the lookup unless 1st 4 characters of surname
     * is quite rare
     */
    private void findMatchesOnPenDemog(PenMatchStudentDetail student, boolean penFoundOnMaster, PenMatchSession session, String localStudentNumber) {
        log.info(" input :: PenMatchStudentDetail={} PenMatchSession={} penFoundOnMaster={} localStudentNumber={}", PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(session), penFoundOnMaster, localStudentNumber);
        boolean useGivenInitial = true;
        boolean type5F1 = false;

        if (student.getPartialSurnameFrequency() <= NOT_VERY_FREQUENT) {
            if (student.getSurname() != null) {
                student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
            }
            useGivenInitial = false;
        } else {
            if (student.getPartialSurnameFrequency() <= VERY_FREQUENT) {
                if (student.getSurname() != null) {
                    student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
                }
                if (student.getGivenName() != null && !student.getGivenName().isEmpty()) {
                    student.setPartialStudentGiven(student.getGivenName().substring(0, 1));
                }
            } else {
                if (student.getSurname() != null) {
                    student.setPartialStudentSurname(student.getSurname().substring(0, student.getMaxSurnameSearchSize()));
                }
                if (student.getGivenName() != null && student.getGivenName().length() >= 2) {
                    student.setPartialStudentGiven(student.getGivenName().substring(0, 2));
                }
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
        performCheckForMatchAndMerge(penDemogList, student, session, localStudentNumber);

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
    private void mergeNewMatchIntoList(PenMatchStudentDetail student, String matchingPEN, PenMatchSession session, PenAlgorithm algorithmUsed, int totalPoints) {
        log.info(" input :: PenMatchStudentDetail={} PenMatchSession={} matchingPEN={} PenAlgorithm={} totalPoints={}", PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(session), matchingPEN, algorithmUsed, totalPoints);
        int matchingAlgorithmResult;
        int matchingScore;

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
                matchingAlgorithmResult = Integer.parseInt(algorithmUsed.getValue()) * 10;
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
            session.getMatchingRecords().add(new PenMatchRecord(matchingAlgorithmResult, matchingScore, matchingPEN));
        }

    }

    /**
     * Check for Matching demographic data on Master
     */
    private CheckForMatchResult checkForMatch(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session) {
        log.info(" input :: PenMatchStudentDetail={} PenMatchSession={} PenMasterRecord={}", PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(session), PenMatchUtils.getJSONFormatObject(master));
        boolean matchFound = false;
        boolean type5F1 = false;
        boolean type5Match = false;
        PenAlgorithm algorithmUsed = null;

        PenMatchUtils.normalizeLocalIDsFromMaster(master);
        PenMatchNames penMatchMasterNames = PenMatchUtils.storeNamesFromMaster(master);

        int totalPoints = 0;
        int bonusPoints;

        int sexPoints = ScoringUtils.matchSex(student, master); // 5 points
        int birthdayPoints = ScoringUtils.matchBirthday(student, master); // 5, 10, 15 or 20 points
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
        int addressPoints = ScoringUtils.matchAddress(student, master); // 1 or 10 points

        // Special search algorithm - just looks for any points in all of
        // the non-blank search fields provided
        if (student.getUpdateCode() != null && student.getUpdateCode().equals("S")) {
            matchFound = true;
            if (student.getSex() != null && sexPoints == 0) {
                matchFound = false;
            }
            if ((student.getSurname() != null || student.getUsualSurname() != null) && surnameMatchResult.getSurnamePoints() == 0) {
                matchFound = false;
            }
            if ((student.getGivenName() != null || student.getUsualGivenName() != null) && givenNameMatchResult.getGivenNamePoints() == 0) {
                matchFound = false;
            }
            if ((student.getMiddleName() != null || student.getUsualMiddleName() != null) && middleNameMatchResult.getMiddleNamePoints() == 0) {
                matchFound = false;
            }
            if (student.getDob() != null && birthdayPoints == 0) {
                matchFound = false;
            }
            if ((student.getLocalID() != null || student.getMincode() != null) && localIDMatchResult.getLocalIDPoints() == 0) {
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

            if (sexPoints >= 5 && birthdayPoints >= 20 && surnameMatchResult.getSurnamePoints() >= 20 && bonusPoints >= 25) {
                matchFound = true;
                session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
                session.setReallyGoodPEN(master.getStudentNumber().trim());
                totalPoints = sexPoints + birthdayPoints + surnameMatchResult.getSurnamePoints() + bonusPoints;
                algorithmUsed = PenAlgorithm.ALG_20;
            }
        }

        // Algorithm 3 : School/ local ID + Surname + 25 bonus points
        // (65 points total)
        if (!matchFound && localIDMatchResult.getLocalIDPoints() >= 20 && surnameMatchResult.getSurnamePoints() >= 20) {
            bonusPoints = sexPoints + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + addressPoints;
            if (bonusPoints >= 25) {
                matchFound = true;
                session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
                session.setReallyGoodPEN(master.getStudentNumber().trim());
                totalPoints = localIDMatchResult.getLocalIDPoints() + surnameMatchResult.getSurnamePoints() + bonusPoints;
                algorithmUsed = PenAlgorithm.ALG_30;
            }
        }

        // Algorithm 4: School/local id + gender + birthdate + 20 bonus points
        // (65 points total)
        if (!matchFound && localIDMatchResult.getLocalIDPoints() >= 20 && sexPoints >= 5 && birthdayPoints >= 20) {
            bonusPoints = surnameMatchResult.getSurnamePoints() + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + addressPoints;
            if (bonusPoints >= 20) {
                matchFound = true;
                session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
                session.setReallyGoodPEN(master.getStudentNumber().trim());
                totalPoints = localIDMatchResult.getLocalIDPoints() + sexPoints + birthdayPoints + bonusPoints;
                algorithmUsed = PenAlgorithm.ALG_40;
            }
        }

        // Algorithm 5: Use points for Sex + birthdate + surname + given name +
        // middle name + address + local_id + school >= 55 bonus points
        if (!matchFound) {
            bonusPoints = sexPoints + birthdayPoints + surnameMatchResult.getSurnamePoints() + givenNameMatchResult.getGivenNamePoints() + middleNameMatchResult.getMiddleNamePoints() + localIDMatchResult.getLocalIDPoints() + addressPoints;
            if (bonusPoints >= localIDMatchResult.getIdDemerits()) {
                bonusPoints = bonusPoints - localIDMatchResult.getIdDemerits();
            } else {
                bonusPoints = 0;
            }

            if (bonusPoints >= 55 || (bonusPoints >= 40 && localIDMatchResult.getLocalIDPoints() >= 20) || (bonusPoints >= 50 && surnameMatchResult.getSurnamePoints() >= 10 && birthdayPoints >= 15 && givenNameMatchResult.getGivenNamePoints() >= 15) || (bonusPoints >= 50 && birthdayPoints >= 20)
                    || (bonusPoints >= 50 && student.getLocalID() != null && student.getLocalID().substring(1, 4).equals("ZZZ"))) {
                matchFound = true;
                algorithmUsed = PenAlgorithm.ALG_50;
                totalPoints = bonusPoints;
                if (bonusPoints >= 70) {
                    session.setReallyGoodMatches(session.getReallyGoodMatches() + 1);
                    session.setReallyGoodPEN(master.getStudentNumber().trim());
                } else if (bonusPoints >= 60 || localIDMatchResult.getLocalIDPoints() >= 20) {
                    session.setPrettyGoodMatches(session.getPrettyGoodMatches() + 1);
                }
                type5F1 = true;
                type5Match = true;
            }
        }

        // Algorithm 5.1: Use points for Sex + birthdate + surname + given name +
        // middle name + address + local_id + school >= 55 bonus points
        if (!matchFound && sexPoints == 5 && birthdayPoints >= 10 && surnameMatchResult.getSurnamePoints() >= 20 && givenNameMatchResult.getGivenNamePoints() >= 10) {
            matchFound = true;
            algorithmUsed = PenAlgorithm.ALG_51;
            totalPoints = 45;

            // Identify a pretty good match - needs to be better than the Questionable Match
            // but not a full 60 points as above
            if (surnameMatchResult.getSurnamePoints() >= 20 && givenNameMatchResult.getGivenNamePoints() >= 15 && birthdayPoints >= 15) {
                session.setPrettyGoodMatches(session.getPrettyGoodMatches() + 1);
                totalPoints = 55;
            }
            type5F1 = true;
            type5Match = true;
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
        log.info(" output :: CheckForMatchResult={}", PenMatchUtils.getJSONFormatObject(result));
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
     */
    private void performCheckForMatchAndMerge(List<PenDemographicsEntity> penDemogList, PenMatchStudentDetail student, PenMatchSession session, String localStudentNumber) {
        log.info(" input :: penDemogList={} PenMatchStudentDetail={} PenMatchSession={} localStudentNumber={}", PenMatchUtils.getJSONFormatObject(penDemogList), PenMatchUtils.getJSONFormatObject(student), PenMatchUtils.getJSONFormatObject(session), localStudentNumber);
        if (penDemogList != null) {
            for (PenDemographicsEntity entity : penDemogList) {
                if (entity.getStudStatus() != null && !entity.getStudStatus().equals(PenStatus.M.getValue()) && !entity.getStudStatus().equals(PenStatus.D.getValue()) && (localStudentNumber == null || !entity.getStudNo().trim().equals(localStudentNumber))) {
                    PenMasterRecord masterRecord = PenMatchUtils.convertPenDemogToPenMasterRecord(entity);
                    CheckForMatchResult result = checkForMatch(student, masterRecord, session);

                    if (result.isMatchFound()) {
                        String matchingPEN;
                        if (result.isType5Match()) {
                            matchingPEN = masterRecord.getStudentNumber().trim() + "?";
                        } else {
                            matchingPEN = masterRecord.getStudentNumber().trim();
                        }
                        mergeNewMatchIntoList(student, matchingPEN, session, result.getAlgorithmUsed(), result.getTotalPoints());
                    }
                }
            }
        }
    }

}
