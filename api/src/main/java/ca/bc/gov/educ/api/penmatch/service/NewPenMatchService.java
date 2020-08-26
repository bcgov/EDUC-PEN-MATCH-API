package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.constants.PenAlgorithm;
import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.*;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchStudentDetail;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import ca.bc.gov.educ.api.penmatch.util.ScoringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NewPenMatchService {

    public static final int VERY_FREQUENT = 500;
    public static final int NOT_VERY_FREQUENT = 50;
    public static final int VERY_RARE = 5;

    @Autowired
    private PenMatchLookupManager lookupManager;

    public NewPenMatchService(PenMatchLookupManager lookupManager) {
        this.lookupManager = lookupManager;
    }

    /**
     * This is the main method to match a student
     */
    public PenMatchResult matchStudent(NewPenMatchStudentDetail student) {
        log.info(" input :: PenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
        NewPenMatchSession session = initialize(student);

        PenConfirmationResult confirmationResult = new PenConfirmationResult();
        confirmationResult.setDeceased(false);

        if (student.getPen() != null) {
            boolean validCheckDigit = PenMatchUtils.penCheckDigit(student.getPen());
            if (validCheckDigit) {
                confirmationResult = confirmPEN(student, session);
                if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_CONFIRMED)) {
                    storeMatches();
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
        log.info(" output :: PenMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
        return result;
    }

    /**
     * Initialize the student record and variables (will be refactored)
     */
    private NewPenMatchSession initialize(NewPenMatchStudentDetail student) {
        log.info(" input :: NewPenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
        NewPenMatchSession session = new NewPenMatchSession();

        if (student.getMincode() != null && student.getMincode().length() >= 3 && student.getMincode().startsWith("102")) {
            session.setPSI(true);
        }

        student.setPenMatchTransactionNames(formatNamesFromTransaction(student));

        // Lookup surname frequency
        // It could generate extra points later if
        // there is a perfect match on surname
        int partialSurnameFrequency;
        String fullStudentSurname = student.getSurname();
        int fullSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);

        if (fullSurnameFrequency > VERY_FREQUENT) {
            partialSurnameFrequency = fullSurnameFrequency;
        } else {
            fullStudentSurname = student.getSurname().substring(0, student.getMinSurnameSearchSize());
            partialSurnameFrequency = lookupManager.lookupSurnameFrequency(fullStudentSurname);
        }

        student.setFullSurnameFrequency(fullSurnameFrequency);
        student.setPartialSurnameFrequency(partialSurnameFrequency);

        log.info(" output :: NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(session));
        return session;
    }

    /**
     * This function stores all names in an object
     */
    private PenMatchNames formatNamesFromTransaction(NewPenMatchStudentDetail student) {
        log.info(" input :: NewPenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
        String surname = student.getSurname();
        String usualSurname = student.getUsualSurname();
        String given = student.getGivenName();
        String usualGiven = student.getUsualGivenName();
        PenMatchNames penMatchTransactionNames;

        penMatchTransactionNames = new PenMatchNames();
        penMatchTransactionNames.setLegalSurname(PenMatchUtils.dropNonLetters(surname));
        penMatchTransactionNames.setLegalGiven(PenMatchUtils.dropNonLetters(given));
        penMatchTransactionNames.setLegalMiddle(PenMatchUtils.dropNonLetters(student.getMiddleName()));
        penMatchTransactionNames.setUsualSurname(PenMatchUtils.dropNonLetters(usualSurname));
        penMatchTransactionNames.setUsualGiven(PenMatchUtils.dropNonLetters(usualGiven));
        return penMatchTransactionNames;
    }

    /**
     * This function stores all names in an object It includes some split logic for
     * given/middle names
     */
    public static PenMatchNames formatNamesFromMaster(PenMasterRecord master) {
        log.info(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
        String surname = master.getSurname();
        String usualSurname = master.getUsualSurname();
        String given = master.getGiven();
        String usualGiven = master.getUsualGivenName();
        PenMatchNames penMatchTransactionNames;

        penMatchTransactionNames = new PenMatchNames();
        penMatchTransactionNames.setLegalSurname(PenMatchUtils.dropNonLetters(surname));
        penMatchTransactionNames.setLegalGiven(PenMatchUtils.dropNonLetters(given));
        penMatchTransactionNames.setLegalMiddle(PenMatchUtils.dropNonLetters(master.getMiddle()));
        penMatchTransactionNames.setUsualSurname(PenMatchUtils.dropNonLetters(usualSurname));
        penMatchTransactionNames.setUsualGiven(PenMatchUtils.dropNonLetters(usualGiven));
        return penMatchTransactionNames;
    }

    /**
     * Confirm that the PEN on transaction is correct.
     */
    private PenConfirmationResult confirmPEN(NewPenMatchStudentDetail student, NewPenMatchSession session) {
        log.info(" input :: NewPenMatchStudentDetail={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
        PenConfirmationResult result = new PenConfirmationResult();
        result.setPenConfirmationResultCode(PenConfirmationResult.NO_RESULT);

        String localStudentNumber = student.getPen();
        result.setDeceased(false);

        PenMasterRecord masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);

        boolean matchFound = false;

        if (masterRecord != null && masterRecord.getStudentNumber().trim().equals(localStudentNumber)) {
            if (masterRecord.getStatus() != null && masterRecord.getStatus().equals("M") && masterRecord.getTrueNumber() != null) {
                localStudentNumber = masterRecord.getTrueNumber().trim();
                result.setMergedPEN(masterRecord.getTrueNumber().trim());
                masterRecord = lookupManager.lookupStudentByPEN(localStudentNumber);
                if (masterRecord != null && masterRecord.getStudentNumber().trim().equals(localStudentNumber)) {
                    result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
                }
            } else {
                result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
            }
        }

        if (result.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
            PenMatchNames masterNames = formatNamesFromMaster(masterRecord);
            determineMatchCode();
        }

        result.setLocalStudentNumber(localStudentNumber);
        result.setMasterRecord(masterRecord);

        log.info(" output :: PenConfirmationResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
        return result;
    }

    /**
     * ---------------------------------------------------------------------------
     * Determine match code based on legal names, birth date and gender
     * ---------------------------------------------------------------------------
     */
    private void determineMatchCode(NewPenMatchStudentDetail student, PenMatchNames masterNames) {
        // ! Match surname
        // ! -------------
        // !
        // ! Possible Values for SURNAME_MATCH_CODE:
        // !       1       Identical, Matches usual or partial (plus overrides to value 2)
        // !       2       Different

        int surnameMatchCode = 0;
        String legalSurname = student.getSurname();
        String usualSurnameNoBlanks = student.getPenMatchTransactionNames().getUsualSurname();
        String legalSurnameNoBlanks = student.getPenMatchTransactionNames().getLegalSurname();
        String legalSurnameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalSurname());
        String masterLegalSurnameNoBlanks = masterNames.getLegalSurname();
        String masterUsualSurnameNoBlanks = masterNames.getUsualSurname();
        String masterLegalSurnameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(masterNames.getLegalSurname());

        // !   submitted legal surname missing (shouldn't happen)
        if (legalSurname != null) {
            surnameMatchCode = 2;
        } else if (masterLegalSurnameNoBlanks != null && masterLegalSurnameNoBlanks.equals(legalSurnameNoBlanks)) {
            // !   submitted legal surname equals master legal surname
            surnameMatchCode = 1;
        } else {
            // !   submitted legal surname is part of master legal surname or vice verse
            String transactionName = " " + legalSurnameHyphenToSpace + " ";
            String masterName = " " + masterLegalSurnameHyphenToSpace + " ";
            if (checkForPartialName(transactionName, masterName)) {
                surnameMatchCode = 1;
            } else {
                surnameMatchCode = 2;
            }
        }

        //!   Overrides: above resulted in match code 2 and
        //!   (submitted legal surname equals master usual surname or
        //!    submitted usual surname equals master legal surname)
        if (surnameMatchCode == 2 && (legalSurnameNoBlanks != null && legalSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) || (usualSurnameNoBlanks != null && usualSurnameNoBlanks.equals(masterLegalSurnameNoBlanks))) {
            surnameMatchCode = 1;
        }

        // ! Match given name
        //! ----------------
        //!
        //! Possible Values for GIVEN_MATCH_CODE:
        //!       1       Identical, nickname or partial (plus overrides to value 2)
        //!       2       Different
        //!       3       Same initial
        //
        //!   submitted legal given name missing (shouldn't happen)
        int givenNameMatchCode = 0;
        String legalGiven = student.getGivenName();
        String legalGivenNoBlanks = student.getPenMatchTransactionNames().getLegalGiven();
        String masterLegalGivenNameNoBlanks = masterNames.getLegalGiven();

        if (legalGiven != null) {
            givenNameMatchCode = 2;
        } else if (masterLegalGivenNameNoBlanks != null && masterLegalGivenNameNoBlanks.equals(legalGivenNoBlanks)) {
            // !   submitted legal given name equals master legal given name
            givenNameMatchCode = 1;
        } else if () {
            // !   submitted legal given name starts with the same letter as master legal given
            // !   name and one of the names has only an initial
        }

    }

    /**
     * @return
     */
    private boolean checkForPartialName(String transactionName, String masterName) {
        boolean partialName = false;
        if (transactionName.contains(masterName) || masterName.contains(transactionName)) {
            return true;
        }

        return false;
    }

}
