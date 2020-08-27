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
    private boolean reOrganizedNames = false;

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
            String matchCode = determineMatchCode(student, masterRecord);

            String matchResult = lookupManager.lookupMatchResult(matchCode);

            if(matchResult == null){
                matchResult = "F";
            }

            if(matchResult.equals("P")){
                result.setPenConfirmationResultCode(PenConfirmationResult.PEN_CONFIRMED);

            }
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
    private String determineMatchCode(NewPenMatchStudentDetail student, PenMasterRecord masterRecord) {
        PenMatchNames masterNames = formatNamesFromMaster(masterRecord);

        // ! Match surname
        // ! -------------
        // !
        // ! Possible Values for SURNAME_MATCH_CODE:
        // !       1       Identical, Matches usual or partial (plus overrides to value 2)
        // !       2       Different

        String surnameMatchCode;
        String legalSurname = student.getSurname();
        String usualSurnameNoBlanks = student.getPenMatchTransactionNames().getUsualSurname();
        String legalSurnameNoBlanks = student.getPenMatchTransactionNames().getLegalSurname();
        String legalSurnameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalSurname());
        String masterLegalSurnameNoBlanks = masterNames.getLegalSurname();
        String masterUsualSurnameNoBlanks = masterNames.getUsualSurname();
        String masterLegalSurnameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(masterNames.getLegalSurname());

        // !   submitted legal surname missing (shouldn't happen)
        if (legalSurname == null) {
            surnameMatchCode = "2";
        } else if (masterLegalSurnameNoBlanks != null && masterLegalSurnameNoBlanks.equals(legalSurnameNoBlanks)) {
            // !   submitted legal surname equals master legal surname
            surnameMatchCode = "1";
        } else {
            // !   submitted legal surname is part of master legal surname or vice verse
            String transactionName = " " + legalSurnameHyphenToSpace + " ";
            String masterName = " " + masterLegalSurnameHyphenToSpace + " ";
            if (checkForPartialName(transactionName, masterName)) {
                surnameMatchCode = "1";
            } else {
                surnameMatchCode = "2";
            }
        }

        //!   Overrides: above resulted in match code 2 and
        //!   (submitted legal surname equals master usual surname or
        //!    submitted usual surname equals master legal surname)
        if (surnameMatchCode.equals("2") && (legalSurnameNoBlanks != null && legalSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) || (usualSurnameNoBlanks != null && usualSurnameNoBlanks.equals(masterLegalSurnameNoBlanks))) {
            surnameMatchCode = "1";
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
        String givenNameMatchCode;
        String legalGiven = student.getGivenName();
        String legalGivenNoBlanks = student.getPenMatchTransactionNames().getLegalGiven();
        String usualGivenNoBlanks = student.getPenMatchTransactionNames().getUsualGiven();
        String legalGivenHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalGiven());
        String masterLegalGivenName = masterRecord.getGiven().trim();
        String masterLegalGivenNameNoBlanks = masterNames.getLegalGiven();
        String masterUsualGivenNameNoBlanks = masterNames.getUsualGiven();
        String masterLegalGivenNameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(masterNames.getLegalGiven());

        if (legalGiven == null) {
            givenNameMatchCode = "2";
        } else if (masterLegalGivenNameNoBlanks != null && masterLegalGivenNameNoBlanks.equals(legalGivenNoBlanks)) {
            // !   submitted legal given name equals master legal given name
            givenNameMatchCode = "1";
        } else if ((legalGiven != null && legalGiven.length() >= 1 && masterLegalGivenName != null && masterLegalGivenName.length() >= 1 && legalGiven.substring(0, 1).equals(masterLegalGivenName.substring(0, 1))) && (masterLegalGivenName.length() == 1 || legalGiven.length() == 1)) {
            // !   submitted legal given name starts with the same letter as master legal given
            // !   name and one of the names has only an initial
            givenNameMatchCode = "3";
        } else {
            // !   submitted legal given name is part of master legal given name or vice verse
            String transactionName = " " + legalGivenHyphenToSpace + " ";
            String masterName = " " + masterLegalGivenNameHyphenToSpace + " ";
            if (checkForPartialName(transactionName, masterName) && !reOrganizedNames) {
                givenNameMatchCode = "1";
            } else {
                // !   submitted legal given name is a nickname of master legal given name or vice
                // !   verse
                transactionName = legalGivenHyphenToSpace;
                masterName = masterLegalGivenNameHyphenToSpace;

                lookupManager.lookupNicknames(student.getPenMatchTransactionNames(), transactionName);

                if (student.getPenMatchTransactionNames().getNickname1() != null) {
                    givenNameMatchCode = "1";
                } else {
                    givenNameMatchCode = "2";
                }
            }
        }

        // !  Overrides: above resulted in surname match code 1 and given name match code 2
        // !  and (submitted legal given name equals master usual given name or
        // !       submitted usual given name equals master legal given name)
        if (surnameMatchCode.equals("1") && givenNameMatchCode.equals("2")) {
            if ((legalGivenNoBlanks != null && legalGivenNoBlanks.equals(masterUsualGivenNameNoBlanks)) || (usualGivenNoBlanks != null && usualGivenNoBlanks.equals(masterLegalGivenNameNoBlanks))) {
                givenNameMatchCode = "1";
            }
        }

        //! Match middle name
        //! -----------------
        //!
        //! Possible Values for MIDDLE_MATCH_CODE:
        //!       1       Identical, nickname or partial
        //!       2       Different
        //!       3       Same initial, one letter typo or one missing
        //!       4       Both missing
        String middleNameMatchCode;
        String legalMiddle = student.getMiddleName();
        String legalMiddleNoBlanks = student.getPenMatchTransactionNames().getLegalMiddle();
        String usualMiddleNoBlanks = student.getPenMatchTransactionNames().getUsualMiddle();
        String legalMiddleHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalMiddle());
        String masterLegalMiddleName = masterRecord.getMiddle().trim();
        String masterLegalMiddleNameNoBlanks = masterNames.getLegalMiddle();
        String masterUsualMiddleNameNoBlanks = masterNames.getUsualMiddle();
        String masterLegalMiddleNameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(masterNames.getLegalMiddle());

        // !   submitted legal middle name and master legal middle name are both blank
        if (legalMiddle == null && masterRecord.getMiddle() == null) {
            middleNameMatchCode = "4";
        } else if (legalMiddle == null || masterRecord.getMiddle() == null) {
            // !   submitted legal middle name or master legal middle is blank (not both)
            middleNameMatchCode = "3";
        } else if (legalMiddleNoBlanks != null && legalMiddleNoBlanks.equals(masterLegalMiddleNameNoBlanks)) {
            // !   submitted legal middle name equals master legal middle name
            middleNameMatchCode = "1";
        } else if ((legalMiddle != null && legalMiddle.length() >= 1 && masterLegalMiddleName != null && masterLegalMiddleName.length() >= 1 && legalMiddle.substring(0, 1).equals(masterLegalMiddleName.substring(0, 1))) && (masterLegalMiddleName.length() == 1 || legalMiddle.length() == 1)) {
            //!   submitted legal middle name starts with the same letter as master legal
            //!   middle name and one of the names has only an initial
            middleNameMatchCode = "3";
        } else {
            // !   submitted legal Middle name is part of master legal Middle name or vice verse
            String transactionName = " " + legalMiddleHyphenToSpace + " ";
            String masterName = " " + masterLegalMiddleNameHyphenToSpace + " ";
            if (checkForPartialName(transactionName, masterName) && !reOrganizedNames) {
                middleNameMatchCode = "1";
            } else {
                // !   submitted legal Middle name is a nickname of master legal Middle name or vice
                // !   verse
                transactionName = legalMiddleHyphenToSpace;
                masterName = masterLegalMiddleNameHyphenToSpace;

                lookupManager.lookupNicknames(student.getPenMatchTransactionNames(), transactionName);

                if (student.getPenMatchTransactionNames().getNickname1() != null) {
                    middleNameMatchCode = "1";
                } else {
                    middleNameMatchCode = "2";
                }
            }
        }

        //! Match birth date
        //! ----------------
        //!
        //! Possible Values for YEAR_MATCH_CODE, MONTH_MATCH_CODE or DAY_MATCH_CODE:
        //!       1       Identical (plus overrides to value 2)
        //!       2       Different
        //
        //!   submitted birth date matches master
        String studentDob = student.getDob();
        String masterDob = masterRecord.getDob();
        String yearMatchCode = null;
        String monthMatchCode = null;
        String dayMatchCode = null;

        if (studentDob != null && studentDob.equals(masterDob)) {
            // !   submitted birth date matches master
            yearMatchCode = "1";
            monthMatchCode = "1";
            dayMatchCode = "1";
        } else if (studentDob != null && studentDob.length() >= 4 && studentDob.substring(0, 4).equals(masterDob.substring(0, 1))) {
            // !   submitted year matches master
            yearMatchCode = "1";
        } else {
            yearMatchCode = "2";
        }

        // !   submitted month matches master
        if (studentDob != null && studentDob.length() >= 6 && studentDob.substring(4, 6).equals(masterDob.substring(4, 6))) {
            monthMatchCode = "1";
        }else{
            monthMatchCode = "2";
        }

        // !   submitted day matches master
        if(studentDob != null && studentDob.length() >= 8 && studentDob.substring(6, 8).equals(masterDob.substring(6, 8))) {
            dayMatchCode = "1";
        }else{
            dayMatchCode = "2";
        }

        String birthdayMatchCode = yearMatchCode + monthMatchCode + dayMatchCode;

        //!   Override:
        //!   only submitted year didn't match master but the last 2 digits are transposed
        if(birthdayMatchCode.equals("211")){
            String tempDobYear = studentDob.substring(3,4) + studentDob.substring(2,3);
            if(tempDobYear.equals(masterDob.substring(2,4))){
                yearMatchCode = "1";
            }
        }else if(birthdayMatchCode.equals("121")){
            // !   Override:
            // !   only submitted month didn't match master but the last 2 digits are transposed
            String tempDobMonth = studentDob.substring(5,6) + studentDob.substring(4,5);
            if(tempDobMonth.equals(masterDob.substring(4,6))){
                monthMatchCode = "1";
            }
        }else if(birthdayMatchCode.equals("112")){
            // !   Override:
            // !   only submitted day didn't match master but the last 2 digits are transposed
            String tempDobDay = studentDob.substring(7,8) + studentDob.substring(6,7);
            if(tempDobDay.equals(masterDob.substring(6,8))){
                dayMatchCode = "1";
            }
        }else if(birthdayMatchCode.equals("122") && studentDob.substring(4,6).equals(masterDob.substring(6,8)) && studentDob.substring(6,8).equals(masterDob.substring(4,6))){
            // !   Override:
            // !   Year matched master but month and day did not and they are transposed
            monthMatchCode = "1";
            dayMatchCode = "1";
        }

        // ! Match gender
        // ! ------------
        // !
        // ! Possible Values for GENDER_MATCH_CODE:
        // !       1       Identical
        // !       2       Different
        String genderMatchCode;
        String studentSex = student.getSex();
        String masterSex = masterRecord.getSex();

        if(studentSex != null && studentSex.equals(masterSex)){
            genderMatchCode = "1";
        }else{
            genderMatchCode = "2";
        }

        String matchCode = surnameMatchCode + givenNameMatchCode + middleNameMatchCode + yearMatchCode + monthMatchCode + dayMatchCode + genderMatchCode;

        return matchCode;
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
