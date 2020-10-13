package ca.bc.gov.educ.api.penmatch.service.match;

import ca.bc.gov.educ.api.penmatch.compare.NewPenMatchComparator;
import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenConfirmationResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.*;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The type New pen match service.
 */
@Service
@Slf4j
public class NewPenMatchService extends BaseMatchService<NewPenMatchStudentDetail, PenMatchResult> {

  /**
   * The constant NOT_VERY_FREQUENT.
   */
  public static final int NOT_VERY_FREQUENT = 50;
  /**
   * The constant VERY_FREQUENT.
   */
  public static final int VERY_FREQUENT = 500;
  /**
   * The constant MIN_SURNAME_COMPARE_SIZE.
   */
  public static final int MIN_SURNAME_COMPARE_SIZE = 5;
  /**
   * The constant MERGED.
   */
  public static final String MERGED = "M";
  /**
   * The One match override main codes.
   */
  private final HashSet<String> oneMatchOverrideMainCodes = new HashSet<>();
  /**
   * The One match override secondary codes.
   */
  private final HashSet<String> oneMatchOverrideSecondaryCodes = new HashSet<>();

  /**
   * The Lookup manager.
   */
  private final PenMatchLookupManager lookupManager;

  /**
   * Instantiates a new New pen match service.
   *
   * @param lookupManager the lookup manager
   */
  @Autowired
  public NewPenMatchService(PenMatchLookupManager lookupManager) {
    this.lookupManager = lookupManager;
    this.setOverrideCodes();
  }

  /**
   * Sets override codes.
   */
  private void setOverrideCodes() {
    oneMatchOverrideMainCodes.add("1111122");
    oneMatchOverrideMainCodes.add("1111212");
    oneMatchOverrideMainCodes.add("1111221");
    oneMatchOverrideMainCodes.add("1112112");
    oneMatchOverrideMainCodes.add("1112211");
    oneMatchOverrideMainCodes.add("1121112");
    oneMatchOverrideMainCodes.add("1122111");
    oneMatchOverrideMainCodes.add("1131121");
    oneMatchOverrideMainCodes.add("1131122");
    oneMatchOverrideMainCodes.add("1131221");
    oneMatchOverrideMainCodes.add("1132111");
    oneMatchOverrideMainCodes.add("1132112");
    oneMatchOverrideMainCodes.add("1141122");
    oneMatchOverrideMainCodes.add("1141212");
    oneMatchOverrideMainCodes.add("1141221");
    oneMatchOverrideMainCodes.add("1211111");
    oneMatchOverrideMainCodes.add("1211112");
    oneMatchOverrideMainCodes.add("1231111");
    oneMatchOverrideMainCodes.add("1231211");
    oneMatchOverrideMainCodes.add("1241111");
    oneMatchOverrideMainCodes.add("1241112");
    oneMatchOverrideMainCodes.add("1241211");
    oneMatchOverrideMainCodes.add("1321111");
    oneMatchOverrideMainCodes.add("2111111");
    oneMatchOverrideMainCodes.add("2111112");
    oneMatchOverrideMainCodes.add("2111121");
    oneMatchOverrideMainCodes.add("2111211");
    oneMatchOverrideMainCodes.add("2112111");
    oneMatchOverrideMainCodes.add("2131111");
    oneMatchOverrideMainCodes.add("2131121");
    oneMatchOverrideMainCodes.add("2131211");
    oneMatchOverrideMainCodes.add("2132111");
    oneMatchOverrideMainCodes.add("2141111");
    oneMatchOverrideMainCodes.add("2141112");
    oneMatchOverrideMainCodes.add("2141211");
    oneMatchOverrideMainCodes.add("2142111");


    oneMatchOverrideSecondaryCodes.add("1131221");
    oneMatchOverrideSecondaryCodes.add("1211111");
    oneMatchOverrideSecondaryCodes.add("1211112");
    oneMatchOverrideSecondaryCodes.add("1231111");
    oneMatchOverrideSecondaryCodes.add("1321111");
    oneMatchOverrideSecondaryCodes.add("2131111");
    oneMatchOverrideSecondaryCodes.add("2132111");
  }

  /**
   * This function stores all names in an object It includes some split logic for
   * given/middle names
   *
   * @param master the master
   * @return the pen match names
   */
  public PenMatchNames formatNamesFromMaster(PenMasterRecord master) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
    }
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
    stopwatch.stop();
    log.info("Completed new PEN match :: formatNamesFromMaster :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return penMatchTransactionNames;
  }

  /**
   * This is the main method to match a student
   */
  public PenMatchResult matchStudent(NewPenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    log.info("Started new match");
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
    }
    NewPenMatchSession session = initialize(student);

    PenConfirmationResult confirmationResult = new PenConfirmationResult();
    confirmationResult.setDeceased(false);

    if (student.getPen() != null) {
      boolean validCheckDigit = PenMatchUtils.penCheckDigit(student.getPen());
      if (validCheckDigit) {
        // Attempt to confirm a supplied PEN
        confirmationResult = confirmPEN(student, session);
        if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_CONFIRMED)) {
          if (student.getStudentTrueNumber() == null) {
            session.setPenStatus(PenStatus.AA.getValue());
          } else {
            session.setPenStatus(PenStatus.B1.getValue());
          }
        } else {
          // Find match using demographics if
          // The supplied PEN was not confirmed or no PEN was supplied
          findMatchesByDemog(student, session);
          if (session.getMatchingRecordsList().size() == 1) {
            NewPenMatchRecord matchRecord = session.getMatchingRecordsList().get(0);
            if (matchRecord.getMatchResult().equals("P")) {
              if (student.getPen() != null && student.getPen().equals(matchRecord.getMatchingPEN())) {
                //PEN confirmed
                session.setPenStatus(PenStatus.AA.getValue());
              } else if (student.getPen() == null) {
                //No PEN Supplied
                session.setPenStatus(PenStatus.D1.getValue());
              } else if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
                //Wrong PEN Supplied
                session.setPenStatus(PenStatus.B1.getValue());
              } else {
                //Invalid PEN Supplied
                session.setPenStatus(PenStatus.C1.getValue());
              }
            } else {
              if (matchRecord.getMatchResult() == null) {
                //Unknown match result
                session.setPenStatus(PenStatus.UR.getValue());
              } else {
                //Single questionable match
                //session.getMatchingRecordsQueue().add(new BestMatchRecord(Long.parseLong("999999999999"), matchRecord.getMatchCode(), matchRecord.getMatchingPEN(), matchRecord.getStudentID()));
                session.setPenStatus(PenStatus.F1.getValue());
              }
            }
          } else if (session.getMatchingRecordsList().size() > 1) {
            if (student.getPen() == null) {
              //No PEN Supplied
              session.setPenStatus(PenStatus.DM.getValue());
            } else if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
              //Wrong PEN Supplied
              session.setPenStatus(PenStatus.BM.getValue());
            } else {
              //Invalid PEN Supplied
              session.setPenStatus(PenStatus.CM.getValue());
            }
            determineBestMatch(session);
          } else {
            //! Assign a new PEN if there were no matches and the flag was passed to assign
            //! new PENS (not just lookup mode) (NO LONGER DONE HERE - NEW PENS NOW ASSIGNED
            //! IN CALLING QUICK PROGRAM VIA ASSIGN_NEW_PEN.USE)
            if (student.getPen() == null) {
              //No PEN Supplied
              session.setPenStatus(PenStatus.D0.getValue());
            } else if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
              //Wrong PEN Supplied
              session.setPenStatus(PenStatus.B0.getValue());
            } else {
              //Invalid PEN Supplied
              session.setPenStatus(PenStatus.C0.getValue());
            }

            if (student.isAssignNewPEN() && session.getPenStatus().equals(PenStatus.B0.getValue())) {
              if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
                session.setPenStatus(PenStatus.G0.getValue());
              }
            }
          }
        }
      }

    }

    PenMatchResult result = new PenMatchResult(PenMatchUtils.convertBestMatchPriorityQueueToList(session.getMatchingRecordsQueue()), session.getPenStatus(), session.getPenStatusMessage());
    if (log.isDebugEnabled()) {
      log.debug(" output :: NewPenMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: matchStudent :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }

  /**
   * Find all possible students on master who could match the transaction.
   * If the first four characters of surname are uncommon then only use 4
   * characters in lookup.  Otherwise use 6 characters, or 5 if surname is
   * only 5 characters long
   * use the given initial in the lookup unless 1st 4 characters of surname is
   * quite rare
   *
   * @param student the student
   * @param session the session
   */
  private void findMatchesByDemog(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    boolean useGiven = true;

    if (student.getPartialSurnameFrequency() <= NOT_VERY_FREQUENT) {
      student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
      useGiven = false;
    } else if (student.getPartialSurnameFrequency() <= VERY_FREQUENT) {
      student.setPartialStudentSurname(student.getSurname().substring(0, student.getMinSurnameSearchSize()));
      student.setPartialStudentGiven(student.getGivenName().substring(0, 1));
    } else {
      student.setPartialStudentSurname(student.getSurname().substring(0, student.getMaxSurnameSearchSize()));
      student.setPartialStudentGiven(student.getGivenName().substring(0, 2));
    }

    if (useGiven) {
      lookupByDobSurnameGiven(student, session);
    } else {
      lookupByDobSurname(student, session);
    }

    //Post-match overrides
    if (session.getMatchingRecordsList().size() == 1 && student.getApplicationCode() != null && student.getApplicationCode().equals("SLD")) {
      oneMatchOverrides(student, session);
    }

    if (!session.getMatchingRecordsList().isEmpty() && student.getApplicationCode() != null && student.getApplicationCode().equals("SLD")) {
      changeResultFromQtoF(student, session);
    }

    appendOldF1(student, session);
    stopwatch.stop();
    log.info("Completed new PEN match :: findMatchesByDemog :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Read Pen master by BIRTH DATE or SURNAME or (MINCODE and LOCAL ID)
   * !---------------------------------------------------------------------------
   *
   * @param student the student
   * @param session the session
   */
  private void lookupByDobSurnameGiven(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    List<StudentEntity> penDemogList = lookupManager.lookupNoLocalID(student.getDob(), student.getPartialStudentSurname(), student.getPartialStudentGiven());
    for (StudentEntity entity : penDemogList) {
      determineIfMatch(student, PenMatchUtils.convertStudentEntityToPenMasterRecord(entity), session);
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: lookupByDobSurnameGiven :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Determine if the match is a Pass or Fail
   * !---------------------------------------------------------------------------
   *
   * @param student      the student
   * @param masterRecord the master record
   * @param session      the session
   */
  private void determineIfMatch(NewPenMatchStudentDetail student, PenMasterRecord masterRecord, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: NewPenMatchStudentDetail={} PenMasterRecord={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(masterRecord), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    String matchCode = determineMatchCode(student, masterRecord, false);

    //Lookup Result
    String matchResult = lookupManager.lookupMatchResult(matchCode);

    //Apply overrides to Questionable Match
    if ("Q".equals(matchResult) && "SLD".equals(student.getApplicationCode())) {
      matchResult = matchOverrides(student, masterRecord, matchCode, matchResult);
    }

    //Store PEN, match code and result in table (except if Fail)
    if (!"F".equals(matchResult) && session.getMatchingRecordsList().size() < 20) {
      if (!"D".equals(masterRecord.getStatus())) {
        session.getMatchingRecordsList().add(new NewPenMatchRecord(matchResult, matchCode, masterRecord.getPen().trim(), masterRecord.getStudentID()));
      } else {
        session.setPenStatus(PenStatus.C0.getValue());
      }
    }
    if (log.isDebugEnabled()) {
      log.debug(" input :: NewPenMatchStudentDetail={} PenMasterRecord={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(masterRecord), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: determineIfMatch :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Read Pen master by BIRTH DATE or (SURNAME AND GIVEN NAME)
   * !                               or (MINCODE and LOCAL ID)
   * !---------------------------------------------------------------------------
   *
   * @param student the student
   * @param session the session
   */
  private void lookupByDobSurname(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    List<StudentEntity> penDemogList = lookupManager.lookupNoInitNoLocalID(student.getDob(), student.getPartialStudentSurname());
    for (StudentEntity entity : penDemogList) {
      determineIfMatch(student, PenMatchUtils.convertStudentEntityToPenMasterRecord(entity), session);
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: lookupByDobSurname :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Override: Change result if there is one match and it meets specific
   * ! criteria for specific match codes
   * !---------------------------------------------------------------------------
   *
   * @param student the student
   * @param session the session
   */
  private void oneMatchOverrides(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    //! 1 match and matched PEN is F1 PEN from the Old PEN Match
    NewPenMatchRecord matchRecord = session.getMatchingRecordsList().get(0);
    if (matchRecord.getMatchResult().equals("Q")) {
      if (student.getOldMatchF1PEN() != null && matchRecord.getMatchingPEN().equals(student.getOldMatchF1PEN())) {
        if (oneMatchOverrideMainCodes.contains(matchRecord.getMatchCode())) {
          matchRecord.setMatchResult("P");
        }
        if (matchRecord.getMatchCode().equals("1221111") && !session.isPSI()) {
          matchRecord.setMatchResult("P");
        }
      } else if (matchRecord.getMatchingPEN().equals(student.getPen()) && oneMatchOverrideSecondaryCodes.contains(matchRecord.getMatchCode())) {
        //! 1 match and matched PEN is the School supplied PEN
        matchRecord.setMatchCode("P");
      }
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: oneMatchOverrides :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * Initialize the student record and variables (will be refactored)
   *
   * @param student the student
   * @return the new pen match session
   */
  private NewPenMatchSession initialize(NewPenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: NewPenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
    }
    NewPenMatchSession session = new NewPenMatchSession();

    if (StringUtils.length(student.getMincode()) > 2  && student.getMincode().startsWith("102")) {
      session.setPSI(true);
    }

    student.setPenMatchTransactionNames(formatNamesFromTransaction(student));
    session.setMatchingRecordsList(new ArrayList<>());
    session.setMatchingRecordsQueue(new PriorityQueue<>(new NewPenMatchComparator()));

    student.setMinSurnameSearchSize(4);
    student.setMaxSurnameSearchSize(6);

    int surnameSize = StringUtils.length(student.getSurname());

    if (surnameSize < student.getMinSurnameSearchSize()) {
      student.setMinSurnameSearchSize(surnameSize);
    }

    if (surnameSize < student.getMaxSurnameSearchSize()) {
      student.setMaxSurnameSearchSize(surnameSize);
    }

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

    if (student.getGivenName() != null) {
      student.setGivenNameNicknames(lookupManager.lookupNicknamesOnly(PenMatchUtils.replaceHyphensWithBlank(student.getGivenName())));
    }

    if (student.getMiddleName() != null) {
      student.setMiddleNameNicknames(lookupManager.lookupNicknamesOnly(PenMatchUtils.replaceHyphensWithBlank(student.getMiddleName())));
    }
    if (log.isDebugEnabled()) {
      log.debug(" output :: NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(session));
    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: initialize :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return session;
  }

  /**
   * This function stores all names in an object
   *
   * @param student the student
   * @return the pen match names
   */
  private PenMatchNames formatNamesFromTransaction(NewPenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: NewPenMatchStudentDetail={}", JsonUtil.getJsonPrettyStringFromObject(student));
    }
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
    stopwatch.stop();
    log.info("Completed new PEN match :: formatNamesFromTransaction :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return penMatchTransactionNames;
  }

  /**
   * Confirm that the PEN on transaction is correct.
   *
   * @param student the student
   * @param session the session
   * @return the pen confirmation result
   */
  private PenConfirmationResult confirmPEN(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: NewPenMatchStudentDetail={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    PenConfirmationResult result = new PenConfirmationResult();
    result.setPenConfirmationResultCode(PenConfirmationResult.NO_RESULT);

    String localStudentNumber = student.getPen();
    result.setDeceased(false);

    var masterRecordOptional = lookupManager.lookupStudentByPEN(localStudentNumber);

    String studentTrueNumber = null;


    if (masterRecordOptional.isPresent() && MERGED.equals(masterRecordOptional.get().getStatus())) {
      studentTrueNumber = lookupManager.lookupStudentTruePENNumberByStudentID(masterRecordOptional.get().getStudentID());
    }

    if (masterRecordOptional.isPresent() && StringUtils.equals(masterRecordOptional.get().getPen(), localStudentNumber)) {
      if (masterRecordOptional.get().getStatus() != null && masterRecordOptional.get().getStatus().equals(MERGED) && studentTrueNumber != null) {
        student.setStudentTrueNumber(studentTrueNumber);
        result.setMergedPEN(studentTrueNumber);
        masterRecordOptional = lookupManager.lookupStudentByPEN(studentTrueNumber);
        if (masterRecordOptional.isPresent() && masterRecordOptional.get().getPen() != null && masterRecordOptional.get().getPen().trim().equals(studentTrueNumber)) {
          result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
        }
      } else {
        result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);
      }
    }

    if (PenConfirmationResult.PEN_ON_FILE.equals(result.getPenConfirmationResultCode()) && masterRecordOptional.isPresent()) {
      String matchCode = determineMatchCode(student, masterRecordOptional.get(), false);
      String matchResult = lookupManager.lookupMatchResult(matchCode);

      if (matchResult.equals("P")) {
        result.setPenConfirmationResultCode(PenConfirmationResult.PEN_CONFIRMED);
        session.getMatchingRecordsList().add(new NewPenMatchRecord(matchResult, matchCode, StringUtils.trimToEmpty(masterRecordOptional.get().getPen()), masterRecordOptional.get().getStudentID()));
      }
    }
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenConfirmationResult={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(result), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: confirmPEN :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }

  /**
   * ---------------------------------------------------------------------------
   * Determine match code based on legal names, birth date and gender
   * ---------------------------------------------------------------------------
   *
   * @param student          the student
   * @param masterRecord     the master record
   * @param reOrganizedNames the re organized names
   * @return the string
   */
  private String determineMatchCode(NewPenMatchStudentDetail student, PenMasterRecord masterRecord, boolean reOrganizedNames) {
    var stopwatch = Stopwatch.createStarted();
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
      if (PenMatchUtils.checkForPartialName(transactionName, masterName)) {
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
    String legalGiven = PenMatchUtils.checkForValidValue(student.getGivenName());
    String legalGivenNoBlanks = student.getPenMatchTransactionNames().getLegalGiven();
    String usualGivenNoBlanks = student.getPenMatchTransactionNames().getUsualGiven();
    String legalGivenHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalGiven());
    String masterLegalGivenName = PenMatchUtils.checkForValidValue(masterRecord.getGiven());
    String masterLegalGivenNameNoBlanks = masterNames.getLegalGiven();
    String masterUsualGivenNameNoBlanks = masterNames.getUsualGiven();
    String masterLegalGivenNameHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(masterNames.getLegalGiven());

    if (legalGiven == null) {
      givenNameMatchCode = "2";
    } else if (masterLegalGivenNameNoBlanks != null && masterLegalGivenNameNoBlanks.equals(legalGivenNoBlanks)) {
      // !   submitted legal given name equals master legal given name
      givenNameMatchCode = "1";
    } else if ((legalGiven.length() >= 1 && masterLegalGivenName != null && masterLegalGivenName.length() >= 1 && legalGiven.substring(0, 1).equals(masterLegalGivenName.substring(0, 1))) && (masterLegalGivenName.length() == 1 || legalGiven.length() == 1)) {
      // !   submitted legal given name starts with the same letter as master legal given
      // !   name and one of the names has only an initial
      givenNameMatchCode = "3";
    } else {
      // !   submitted legal given name is part of master legal given name or vice verse
      String transactionName = " " + legalGivenHyphenToSpace + " ";
      String masterName = " " + masterLegalGivenNameHyphenToSpace + " ";
      if (PenMatchUtils.checkForPartialName(transactionName, masterName) && !reOrganizedNames) {
        givenNameMatchCode = "1";
      } else {
        // !   submitted legal given name is a nickname of master legal given name or vice
        // !   verse
        transactionName = legalGivenHyphenToSpace;
        masterName = masterLegalGivenNameHyphenToSpace;

        List<NicknamesEntity> nicknamesEntities = student.getGivenNameNicknames();

        if (reOrganizedNames) {
          nicknamesEntities = lookupManager.lookupNicknamesOnly(transactionName);
        }

        boolean nicknameMasterMatchFound = false;
        for (NicknamesEntity entity : nicknamesEntities) {
          if (entity.getNickname1().equals(masterLegalGivenNameHyphenToSpace) || entity.getNickname2().equals(masterLegalGivenNameHyphenToSpace)) {
            nicknameMasterMatchFound = true;
            break;
          }
        }

        if (nicknameMasterMatchFound) {
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
    String legalMiddle = PenMatchUtils.checkForValidValue(student.getMiddleName());
    String legalMiddleNoBlanks = student.getPenMatchTransactionNames().getLegalMiddle();
    String legalMiddleHyphenToSpace = PenMatchUtils.replaceHyphensWithBlank(student.getPenMatchTransactionNames().getLegalMiddle());
    String masterLegalMiddleName = PenMatchUtils.checkForValidValue(masterRecord.getMiddle());
    String masterLegalMiddleNameNoBlanks = masterNames.getLegalMiddle();
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
      //!   submitted legal middle name differs from master legal middle name by only
      //!   one character and both names are at least 5 characters long
      String transactionName = " " + legalMiddleNoBlanks + " ";
      String masterName = " " + masterLegalMiddleNameNoBlanks + " ";
      if (oneCharTypo(transactionName, masterName)) {
        middleNameMatchCode = "3";
      } else {
        // !   submitted legal Middle name is part of master legal Middle name or vice verse
        transactionName = " " + legalMiddleHyphenToSpace + " ";
        masterName = " " + masterLegalMiddleNameHyphenToSpace + " ";
        if (PenMatchUtils.checkForPartialName(transactionName, masterName) && !reOrganizedNames) {
          middleNameMatchCode = "1";
        } else {
          // !   submitted legal Middle name is a nickname of master legal Middle name or vice
          // !   verse
          transactionName = legalMiddleHyphenToSpace;
          masterName = masterLegalMiddleNameHyphenToSpace;

          List<NicknamesEntity> nicknamesEntities = student.getMiddleNameNicknames();

          if (reOrganizedNames) {
            nicknamesEntities = lookupManager.lookupNicknamesOnly(transactionName);
          }

          boolean nicknameMasterMatchFound = false;
          for (NicknamesEntity entity : nicknamesEntities) {
            if (entity.getNickname1().equals(masterLegalGivenNameHyphenToSpace) || entity.getNickname2().equals(masterLegalGivenNameHyphenToSpace)) {
              nicknameMasterMatchFound = true;
              break;
            }
          }

          if (nicknameMasterMatchFound) {
            middleNameMatchCode = "1";
          } else {
            middleNameMatchCode = "2";
          }
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
    } else {
      monthMatchCode = "2";
    }

    // !   submitted day matches master
    if (studentDob != null && studentDob.length() >= 8 && studentDob.substring(6, 8).equals(masterDob.substring(6, 8))) {
      dayMatchCode = "1";
    } else {
      dayMatchCode = "2";
    }

    String birthdayMatchCode = yearMatchCode + monthMatchCode + dayMatchCode;

    //!   Override:
    //!   only submitted year didn't match master but the last 2 digits are transposed
    if (birthdayMatchCode.equals("211")) {
      String tempDobYear = studentDob.substring(3, 4) + studentDob.substring(2, 3);
      if (tempDobYear.equals(masterDob.substring(2, 4))) {
        yearMatchCode = "1";
      }
    } else if (birthdayMatchCode.equals("121")) {
      // !   Override:
      // !   only submitted month didn't match master but the last 2 digits are transposed
      String tempDobMonth = studentDob.substring(5, 6) + studentDob.substring(4, 5);
      if (tempDobMonth.equals(masterDob.substring(4, 6))) {
        monthMatchCode = "1";
      }
    } else if (birthdayMatchCode.equals("112")) {
      // !   Override:
      // !   only submitted day didn't match master but the last 2 digits are transposed
      String tempDobDay = studentDob.substring(7, 8) + studentDob.substring(6, 7);
      if (tempDobDay.equals(masterDob.substring(6, 8))) {
        dayMatchCode = "1";
      }
    } else if (birthdayMatchCode.equals("122") && studentDob.substring(4, 6).equals(masterDob.substring(6, 8)) && studentDob.substring(6, 8).equals(masterDob.substring(4, 6))) {
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

    if (studentSex != null && studentSex.equals(masterSex)) {
      genderMatchCode = "1";
    } else {
      genderMatchCode = "2";
    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: determineMatchCode :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return surnameMatchCode + givenNameMatchCode + middleNameMatchCode + yearMatchCode + monthMatchCode + dayMatchCode + genderMatchCode;
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Check to see if both submitted and master names are at least x characters
   * ! long (where x = MIN_SURNAME_COMPARE_SIZE) and different by only one character
   * !---------------------------------------------------------------------------
   *
   * @param transactionName the transaction name
   * @param masterName      the master name
   * @return the boolean
   */
  public boolean oneCharTypo(String transactionName, String masterName) {
    var stopwatch = Stopwatch.createStarted();
    int transactionNameLength = transactionName.length();
    int masterNameLength = masterName.length();

    int nameLengthDiff;
    if (transactionNameLength > masterNameLength) {
      nameLengthDiff = transactionNameLength - masterNameLength;
    } else {
      nameLengthDiff = masterNameLength - transactionNameLength;
    }

    int loopLimit = 0;
    if (transactionNameLength >= MIN_SURNAME_COMPARE_SIZE && masterNameLength >= MIN_SURNAME_COMPARE_SIZE && nameLengthDiff < 2) {
      if (transactionNameLength > masterNameLength) {
        loopLimit = transactionNameLength;
      } else {
        loopLimit = masterNameLength;
      }

      int diffCharCount = 0;
      int transactionNamePosition = 0;
      int masterNamePosition = 0;
      for (int i = 0; i < loopLimit; i++) {
        if (transactionName.charAt(transactionNamePosition) != masterName.charAt(masterNamePosition)) {
          diffCharCount++;
          if (masterNameLength > transactionNameLength) {
            //! Shift 1 char in master
            masterNamePosition++;
            //! prevent another master only shift
            masterNameLength = transactionNameLength;
          } else if (masterNameLength < transactionNameLength) {
            //! shift 1 char in tran
            transactionNamePosition++;
            //! prevent another tran only shift
            transactionNameLength = masterNameLength;
          } else {
            //! shift 1 char in both
            //! tran and master
            transactionNamePosition++;
            masterNamePosition++;
          }
        } else {
          //! shift 1 char in both
          //! tran and master
          transactionNamePosition++;
          masterNamePosition++;
        }
      }

      if (diffCharCount == 1) {
        return true;
      }

    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: oneCharTypo :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return false;
  }


  /**
   * !---------------------------------------------------------------------------
   * ! Overrides that apply immediately after a Match Code is calculated.
   * !---------------------------------------------------------------------------
   *
   * @param student        the student
   * @param masterRecord   the master record
   * @param matchCode      the match code
   * @param curMatchResult the cur match result
   * @return the string
   */
  private String matchOverrides(NewPenMatchStudentDetail student, PenMasterRecord masterRecord, String matchCode, String curMatchResult) {
    var stopwatch = Stopwatch.createStarted();
    String matchResult = curMatchResult;
    //!   Combine given and middle names and re-calculate match code
    if (matchCode.equals("1131211") || matchCode.equals("1131221") || matchCode.equals("1132111") || matchCode.equals("1231111") && (student.getMiddleName() != null || masterRecord.getMiddle() != null)) {
      concatenateNamesAndRecalc(student, masterRecord);
    }

    //!   Switch given and middle names and re-calculate match code
    if (matchCode.equals("1221111") && (student.getMiddleName() != null && masterRecord.getMiddle() != null)) {
      NewPenMatchNameChangeResult switchNamesResult = switchNamesAndRecalc(student, masterRecord);
      if (switchNamesResult != null) {
        matchCode = switchNamesResult.getMatchCode();
        matchResult = switchNamesResult.getMatchResult();
      }
    }

    //!   Pass if Master PEN is F1 PEN from Old Match AND School Supplied PEN
    if (matchCode.equals("1131221") || matchCode.equals("1211111") || matchCode.equals("1221121") && (masterRecord.getPen().equals(student.getOldMatchF1PEN()) && masterRecord.getPen().equals(student.getPen()))) {
      matchResult = "P";
    }

    //!   Pass if Master PEN is F1 PEN from Old Match OR School Supplied PEN else Fail
    if (matchCode.equals("1131211") || matchCode.equals("1142111")) {
      if (masterRecord.getPen().equals(student.getOldMatchF1PEN()) || masterRecord.getPen().equals(student.getPen())) {
        matchResult = "P";
      } else {
        matchResult = "F";
      }
    }

    //!   Fail if Master PEN is NOT F1 PEN from Old Match AND NOT School Supplied PEN
    if (matchCode.equals("1132121") || matchCode.equals("1221111") || matchCode.equals("2131111") && (!masterRecord.getPen().equals(student.getOldMatchF1PEN()) && !masterRecord.getPen().equals(student.getPen()))) {
      matchResult = "F";
    }

    //!   Pass if Master PEN is School Supplied PEN
    if (matchCode.equals("1112121") || matchCode.equals("1131121") || matchCode.equals("1132111") || matchCode.equals("1132121") || matchCode.equals("1141221") || matchCode.equals("1241111") || matchCode.equals("2111111") && (masterRecord.getPen().equals(student.getPen()))) {
      matchResult = "P";
    }

    //!   Pass if Master PEN is School Supplied PEN and Master Surname is NOT
    //!   East Indian
    if (matchCode.equals("1111221") && masterRecord.getPen().equals(student.getPen())) {
      boolean nameFound = lookupManager.lookupForeignSurname(masterRecord.getSurname(), "E IND");
      if (!nameFound) {
        matchResult = "P";
      }
    }

    //!   Fail if Master PEN is NOT F1 PEN from Old Match AND NOT School Supplied PEN
    //!   AND surname is Asian
    if (matchCode.equals("1131121") || matchCode.equals("1241111") && (!masterRecord.getPen().equals(student.getPen())) && !masterRecord.getPen().equals(student.getOldMatchF1PEN())) {
      boolean nameFound = lookupManager.lookupForeignSurname(masterRecord.getSurname(), "ASIAN");
      if (nameFound) {
        matchResult = "F";
      }
    }

    //!   Fail if Master PEN is NOT F1 PEN from Old Match AND NOT School Supplied PEN
    //!   AND (Master Surname is Asian OR East Indian)
    if (matchCode.equals("1131221") || matchCode.equals("1231111") && (!masterRecord.getPen().equals(student.getPen())) && !masterRecord.getPen().equals(student.getOldMatchF1PEN())) {
      boolean nameFound = lookupManager.lookupForeignSurname(masterRecord.getSurname(), "ASIAN");
      if (nameFound) {
        matchResult = "F";
      } else {
        nameFound = lookupManager.lookupForeignSurname(masterRecord.getSurname(), "E IND");
        if (nameFound) {
          matchResult = "F";
        }
      }
    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: matchOverrides :: in  {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return matchResult;
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Override: Change result from Q to F for specific match codes if the
   * ! transaction meets specific criteria and drop the fails from the list (array)
   * !---------------------------------------------------------------------------
   *
   * @param student the student
   * @param session the session
   */
  public void changeResultFromQtoF(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    //!   Change result from Questionable to Fail
    //!   Remove codes from the array if result is Fail
    var filteredList = session.getMatchingRecordsList().stream().filter(el -> !("Q".equals(el.getMatchResult()) && ("1241112".equals(el.getMatchCode()) || "2132111".equals(el.getMatchCode())))).collect(Collectors.toList());
    session.setMatchingRecordsList(filteredList);

    if (session.getMatchingRecordsList().size() == 1) {
      oneMatchOverrides(student, session);
      NewPenMatchRecord record = session.getMatchingRecordsList().get(0);
      if (StringUtils.length(record.getMatchResult()) > 0) {
        record.setMatchResult("Q");
      }
    }
    stopwatch.stop();
    log.info("Completed new PEN match  :: changeResultFromQtoF :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }


  /**
   * !---------------------------------------------------------------------------
   * ! Override: Check the list of matches for the F1 PEN from the Old PEN Match
   * ! and add it to the list if it is not already there. Replace the last match
   * ! in the list with the F1 PEN if the list is full (20 matches). Set the
   * ! result of the added match to 'Questionable'.
   * !---------------------------------------------------------------------------
   *
   * @param student the student
   * @param session the session
   */
  private void appendOldF1(NewPenMatchStudentDetail student, NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    boolean penF1Found;
    if (student.getOldMatchF1PEN() != null) {
      penF1Found = false;
      if (!session.getMatchingRecordsList().isEmpty()) {
        NewPenMatchRecord[] matchRecords = session.getMatchingRecordsList().toArray(new NewPenMatchRecord[session.getMatchingRecordsList().size()]);
        for (NewPenMatchRecord record : matchRecords) {
          if (record.getMatchingPEN().equals(student.getOldMatchF1PEN())) {
            penF1Found = true;
            break;
          }
        }

        if (!penF1Found) {
          if (session.getMatchingRecordsList().size() < 20) {
            session.getMatchingRecordsList().add(new NewPenMatchRecord("Q", "Old F1", student.getOldMatchF1PEN(), student.getOldMatchF1StudentID()));
          }
        }
      }
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: appendOldF1 ::in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * !---------------------------------------------------------------------------
   * ! Determine the 'Best Match' when there are multiple matched.
   * !
   * ! The rules for determining the 'Best Match" are:
   * ! Sum the 7 positions of Match Code. The 'Best Match' has the lowest value.
   * ! If there are ties:
   * ! The Match Code with the most ones is the 'Best Match'.
   * ! If there are ties:
   * ! The 'Match Code' with the most twos is the 'Best Match.
   * ! If there are ties:
   * ! The 'Match Code' with the most threes is the 'Best Match.
   * ! If there are ties:
   * ! The lowest Match Code value is the 'Best Match'.
   * !
   * ! The easiest way to select the 'Best Match' is to put the result of all of
   * ! the above calculations into one comparable value. To do this we need to
   * ! convert the number of ones, twos and threes into their inverted values by
   * ! by subtracting each from 7 (the maximum). Now the 'Best Match' will
   * ! have the lowest value from all of the above calculations allowing us to
   * ! concatenate the results and select the Match Code with the lowest concatenated
   * ! value. This allows us to loop through all found Match Codes calculating the
   * ! concatenated value and saving it and the applicable Match Code/PEN whenever
   * ! the concatenated value is less than the previously saved value.
   * !---------------------------------------------------------------------------
   *
   * @param session the session
   */
  private void determineBestMatch(NewPenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    for (NewPenMatchRecord record : session.getMatchingRecordsList()) {
      String matchCode = record.getMatchCode();
      if (matchCode != null && !matchCode.equals("Old F1")) {
        int sumMatchCode = getSumOfMatchCode(matchCode);
        String sumMatchCodeString = Integer.toString(sumMatchCode);
        if (sumMatchCode < 10) {
          sumMatchCodeString = "0" + sumMatchCodeString;
        }

        int num1 = 0;
        int num2 = 0;
        int num3 = 0;

        for (int i = 0; i < matchCode.length(); i++) {
          if (matchCode.charAt(i) == '1') {
            num1 = num1 + 1;
          } else if (matchCode.charAt(i) == '2') {
            num2 = num2 + 2;
          } else if (matchCode.charAt(i) == '3') {
            num3 = num3 + 3;
          }
        }

        String concatValue = sumMatchCodeString + (7 - num1) + (7 - num2) + (7 - num3) + matchCode;

        session.getMatchingRecordsQueue().add(new BestMatchRecord(Long.parseLong(concatValue), matchCode, record.getMatchingPEN(), record.getStudentID()));
      } else {
        session.getMatchingRecordsQueue().add(new BestMatchRecord(Long.parseLong("999999999999"), matchCode, record.getMatchingPEN(), record.getStudentID()));
      }
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: determineBestMatch :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * Small utility to sum match codes
   *
   * @param matchCode the match code
   * @return the sum of match code
   */
  public int getSumOfMatchCode(String matchCode) {
    return Pattern.compile("")
        .splitAsStream(matchCode)
        .mapToInt(Integer::parseInt)
        .sum();
  }


  /**
   * !---------------------------------------------------------------------------
   * !   Override. Combine given and middle names and re-calculate match code
   * !   (for specific match codes)
   * !---------------------------------------------------------------------------
   * !   Re-calculate match code after combining legal given name and middle name
   * !   into legal given name. If the new match code does not result in a pass then
   * !   re-calculate the match code once again after combining legal middle name
   * !   and given name into legal given name.
   * !   Do this with the transaction names and if still no match , the names
   * !   in the master. If the new match code still does not result in a pass then
   * !   restore the original match code and result.
   *
   * @param student      the student
   * @param masterRecord the master record
   * @return the new pen match name change result
   */
  private NewPenMatchNameChangeResult concatenateNamesAndRecalc(NewPenMatchStudentDetail student, PenMasterRecord masterRecord) {
    var stopwatch = Stopwatch.createStarted();
    String savedGiven = student.getPenMatchTransactionNames().getLegalGiven();
    String savedMiddle = student.getPenMatchTransactionNames().getLegalMiddle();
    String matchResult = null;
    String matchCode = null;

    if (student.getMiddleName() != null) {
      student.getPenMatchTransactionNames().setLegalGiven(savedGiven + savedMiddle);
      student.getPenMatchTransactionNames().setLegalMiddle(null);
      matchCode = determineMatchCode(student, masterRecord, true);
      matchResult = lookupManager.lookupMatchResult(matchCode);

      if (!matchResult.equals("P")) {
        student.getPenMatchTransactionNames().setLegalGiven(savedMiddle + savedGiven);
        matchCode = determineMatchCode(student, masterRecord, true);
        matchResult = lookupManager.lookupMatchResult(matchCode);
      }

      student.getPenMatchTransactionNames().setLegalGiven(savedGiven);
      student.getPenMatchTransactionNames().setLegalMiddle(savedMiddle);
    }

    if (matchResult != null && !matchResult.equals("P") && masterRecord.getMiddle() != null) {
      savedGiven = masterRecord.getGiven();
      savedMiddle = masterRecord.getMiddle();

      masterRecord.setGiven(savedGiven + savedMiddle);
      masterRecord.setMiddle(null);
      matchCode = determineMatchCode(student, masterRecord, true);
      matchResult = lookupManager.lookupMatchResult(matchCode);

      if (!matchResult.equals("P")) {
        masterRecord.setGiven(savedMiddle + savedGiven);
        matchCode = determineMatchCode(student, masterRecord, true);
        matchResult = lookupManager.lookupMatchResult(matchCode);
      }
    }

    if (!"P".equals(matchResult)) {
      return new NewPenMatchNameChangeResult(matchResult, matchCode);
    }
    stopwatch.stop();
    log.info("Completed new PEN match :: concatenateNamesAndRecalc :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return null;
  }

  /**
   * !---------------------------------------------------------------------------
   * !   Override. Switch given and middle names and re-calculate match code
   * !   (for specific match codes)
   * !---------------------------------------------------------------------------
   * !   Re-calculate match code after switching legal middle name and given name
   * !   in the transaction. If the new match code does not result in a pass then
   * !   restore the original match code and result.
   *
   * @param student      the student
   * @param masterRecord the master record
   * @return the new pen match name change result
   */
  private NewPenMatchNameChangeResult switchNamesAndRecalc(NewPenMatchStudentDetail student, PenMasterRecord masterRecord) {
    var stopwatch = Stopwatch.createStarted();
    String legalGiven = student.getPenMatchTransactionNames().getLegalGiven();
    student.getPenMatchTransactionNames().setLegalGiven(student.getPenMatchTransactionNames().getLegalMiddle());
    student.getPenMatchTransactionNames().setLegalMiddle(legalGiven);

    String matchCode = determineMatchCode(student, masterRecord, true);
    String matchResult = lookupManager.lookupMatchResult(matchCode);

    legalGiven = student.getPenMatchTransactionNames().getLegalGiven();
    student.getPenMatchTransactionNames().setLegalGiven(student.getPenMatchTransactionNames().getLegalMiddle());
    student.getPenMatchTransactionNames().setLegalMiddle(legalGiven);

    if (!"P".equals(matchResult)) {
      return new NewPenMatchNameChangeResult(matchResult, matchCode);
    }
    stopwatch.stop();
    log.info("Completed new PEN Match :: switchNamesAndRecalc :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return null;
  }

}
