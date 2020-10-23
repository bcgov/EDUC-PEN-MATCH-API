package ca.bc.gov.educ.api.penmatch.service.match;

import ca.bc.gov.educ.api.penmatch.compare.PenMatchComparator;
import ca.bc.gov.educ.api.penmatch.constants.PenAlgorithm;
import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.*;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchStudentDetail;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import ca.bc.gov.educ.api.penmatch.util.ScoringUtils;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * The type Pen match service.
 */
@Service
@Slf4j
public class PenMatchService extends BaseMatchService<PenMatchStudentDetail, PenMatchResult> {

  /**
   * The constant VERY_FREQUENT.
   */
  public static final int VERY_FREQUENT = 500;
  /**
   * The constant NOT_VERY_FREQUENT.
   */
  public static final int NOT_VERY_FREQUENT = 50;
  /**
   * The constant VERY_RARE.
   */
  public static final int VERY_RARE = 5;
  /**
   * The constant INPUT_PEN_MATCH_STUDENT_DETAIL.
   */
  public static final String INPUT_PEN_MATCH_STUDENT_DETAIL = " input :: PenMatchStudentDetail={}";
  /**
   * The constant MERGED.
   */
  public static final String MERGED = "M";
  /**
   * The constant DECEASED.
   */
  public static final String DECEASED = "D";

  /**
   * The Lookup manager.
   */
  private final PenMatchLookupManager lookupManager;

  /**
   * The New pen match service.
   */
  private final NewPenMatchService newPenMatchService;

  /**
   * Instantiates a new Pen match service.
   *
   * @param lookupManager      the lookup manager
   * @param newPenMatchService the new pen match service
   */
  @Autowired
  public PenMatchService(PenMatchLookupManager lookupManager, NewPenMatchService newPenMatchService) {
    this.lookupManager = lookupManager;
    this.newPenMatchService = newPenMatchService;
  }

  /**
   * This is the main method to match a student
   */
  @Override
  public PenMatchResult matchStudent(PenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    log.info("Started old PEN match");
    if (log.isDebugEnabled()) {
      log.debug(INPUT_PEN_MATCH_STUDENT_DETAIL, JsonUtil.getJsonPrettyStringFromObject(student));
    }
    PenMatchSession session = initialize(student);

    PenConfirmationResult confirmationResult = new PenConfirmationResult();
    confirmationResult.setDeceased(false);

    confirmationResult = setPenStatusByPen(student, session, confirmationResult);

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
    updateStatusToG0BasedOnConditions(student, session);

    checkForF1StatusAndProcess(student, session);

    if (confirmationResult.isDeceased()) {
      session.setPenStatus(PenStatus.C0.getValue());
    }

    PenMatchResult result;

    if (!session.getPenStatus().contains("1") && !session.getPenStatus().equals(PenStatus.AA.getValue())) {
      PenMatchRecord record = new PenMatchRecord();
      if (!session.getMatchingRecords().isEmpty()) {
        record = Objects.requireNonNull(session.getMatchingRecords().peek());
      }
      NewPenMatchStudentDetail newStudentDetail = new NewPenMatchStudentDetail(student, record.getMatchingPEN(), record.getStudentID());
      if (log.isDebugEnabled()) {
        log.debug(" Running new PEN match algorithm with payload: {}", JsonUtil.getJsonPrettyStringFromObject(newStudentDetail));
      }
      stopwatch.stop();
      log.info("Completed old PEN match in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
      return newPenMatchService.matchStudent(newStudentDetail);
    } else {
      result = new PenMatchResult(PenMatchUtils.convertOldMatchPriorityQueueToList(session.getMatchingRecords()), session.getPenStatus(), session.getPenStatusMessage());
    }

    if (log.isDebugEnabled()) {
      log.debug(" output :: PenMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    stopwatch.stop();
    log.info("Completed old PEN match in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }


  private void checkForF1StatusAndProcess(PenMatchStudentDetail student, PenMatchSession session) {
    if (session.getPenStatus().equals(PenStatus.AA.getValue()) || session.getPenStatus().equals(PenStatus.B1.getValue()) || session.getPenStatus().equals(PenStatus.C1.getValue()) || session.getPenStatus().equals(PenStatus.D1.getValue())) {
      Optional<PenMasterRecord> masterRecord = lookupManager.lookupStudentByPEN(student.getPen());
      if (masterRecord.isPresent() && !StringUtils.equals(masterRecord.get().getDob(), student.getDob())) {
        session.setPenStatusMessage("Birthdays are suspect: " + masterRecord.get().getDob() + " vs " + student.getDob());
        session.setPenStatus(PenStatus.F1.getValue());
        session.getMatchingRecords().add(new OldPenMatchRecord(null, null, student.getPen(), masterRecord.get().getStudentID()));
      } else if (masterRecord.isPresent() && StringUtils.equals(masterRecord.get().getSurname(), student.getSurname())
          && !StringUtils.equals(masterRecord.get().getGiven(), student.getGivenName())
          && StringUtils.equals(masterRecord.get().getDob(), student.getDob())
          && StringUtils.equals(masterRecord.get().getMincode(), student.getMincode())
          && masterRecord.get().getLocalId() != null
          && student.getLocalID() != null
          && StringUtils.equals(masterRecord.get().getLocalId().trim(), student.getLocalID().trim())) {
        session.setPenStatusMessage("Possible twin: " + masterRecord.get().getGiven().trim() + " vs " + student.getGivenName().trim());
        session.setPenStatus(PenStatus.F1.getValue());
        session.getMatchingRecords().add(new OldPenMatchRecord(null, null, student.getPen(), masterRecord.get().getStudentID()));
      }
    }
  }

  private void updateStatusToG0BasedOnConditions(PenMatchStudentDetail student, PenMatchSession session) {
    if ((session.getPenStatus().equals(PenStatus.C0.getValue()) || session.getPenStatus().equals(PenStatus.D0.getValue()))
        && (student.getUpdateCode() != null
        && (student.getUpdateCode().equals("Y") || student.getUpdateCode().equals("R")))) {
      PenMatchUtils.checkForCoreData(student, session);
    }
  }

  private PenConfirmationResult setPenStatusByPen(PenMatchStudentDetail student, PenMatchSession session, PenConfirmationResult confirmationResult) {
    if (student.getPen() != null) {
      boolean validCheckDigit = PenMatchUtils.penCheckDigit(student.getPen());
      if (validCheckDigit) {
        confirmationResult = getPenConfirmationResult(student, session);

      } else {
        session.setPenStatus(PenStatus.C.getValue());
        findMatchesOnPenDemog(student, false, session, null);
      }
    } else {
      session.setPenStatus(PenStatus.D.getValue());
      findMatchesOnPenDemog(student, false, session, null);
    }
    return confirmationResult;
  }

  private PenConfirmationResult getPenConfirmationResult(PenMatchStudentDetail student, PenMatchSession session) {
    PenConfirmationResult confirmationResult;
    confirmationResult = confirmPEN(student, session);
    if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_CONFIRMED)) {
      if (confirmationResult.getMergedPEN() == null) {
        session.setPenStatus(PenStatus.AA.getValue());
        session.getMatchingRecords().add(new OldPenMatchRecord(null, null, confirmationResult.getMasterRecord().getPen().trim(), confirmationResult.getMasterRecord().getStudentID()));
      } else {
        session.setPenStatus(PenStatus.B1.getValue());
        session.getMatchingRecords().add(new OldPenMatchRecord(null, null, confirmationResult.getMergedPEN(), confirmationResult.getMasterRecord().getStudentID()));
      }
    } else if (confirmationResult.getPenConfirmationResultCode().equals(PenConfirmationResult.PEN_ON_FILE)) {
      session.setPenStatus(PenStatus.B.getValue());
      if (confirmationResult.getMasterRecord().getPen() != null) {
        findMatchesOnPenDemog(student, true, session, confirmationResult.getMasterRecord());
      }
    } else {
      session.setPenStatus(PenStatus.C.getValue());
      findMatchesOnPenDemog(student, false, session, null);
    }
    return confirmationResult;
  }

  /**
   * Initialize the student record and variables (will be refactored)
   *
   * @param student the student
   * @return the pen match session
   */
  private PenMatchSession initialize(PenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(INPUT_PEN_MATCH_STUDENT_DETAIL, JsonUtil.getJsonPrettyStringFromObject(student));
    }
    PenMatchSession session = new PenMatchSession();
    session.setPenStatusMessage(null);
    session.setMatchingRecords(new PriorityQueue<>(new PenMatchComparator()));

    PenMatchUtils.upperCaseInputStudent(student);
    student.setAlternateLocalID("TTT");

    // Strip off leading zeros, leading blanks and trailing blanks
    // from the local_id. Put result in alternateLocalID.
    if (StringUtils.isNotBlank(student.getLocalID())) {
      student.setAlternateLocalID(StringUtils.stripStart(student.getLocalID(), "0").replace(" ", ""));
    }

    student.setPenMatchTransactionNames(storeNamesFromTransaction(student));

    student.setMinSurnameSearchSize(4);
    student.setMaxSurnameSearchSize(6);

    int surnameSize;

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
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(session));
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: initialize :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return session;
  }

  /**
   * This function stores all names in an object It includes some split logic for
   * given/middle names
   *
   * @param student the student
   * @return the pen match names
   */
  private PenMatchNames storeNamesFromTransaction(PenMatchStudentDetail student) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(INPUT_PEN_MATCH_STUDENT_DETAIL, JsonUtil.getJsonPrettyStringFromObject(student));
    }
    String surname = student.getSurname();
    String usualSurname = student.getUsualSurname();
    String given = student.getGivenName();
    String usualGiven = student.getUsualGivenName();
    PenMatchNames penMatchTransactionNames;

    penMatchTransactionNames = new PenMatchNames();
    penMatchTransactionNames.setLegalSurname(surname);
    penMatchTransactionNames.setUsualSurname(usualSurname);
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
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenMatchNames={}", JsonUtil.getJsonPrettyStringFromObject(penMatchTransactionNames));
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: storeNamesFromTransaction :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return penMatchTransactionNames;
  }

  /**
   * Check for exact match on surname , given name, birthday and gender OR exact
   * match on school and local ID and one or more of surname, given name or
   * birthday
   *
   * @param student the student
   * @param master  the master
   * @param session the session
   * @return the check for match result
   */
  private CheckForMatchResult simpleCheckForMatch(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={} PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    boolean matchFound = false;
    PenAlgorithm algorithmUsed = null;

    if (isS1Match(student, master)) {
      matchFound = true;
      algorithmUsed = PenAlgorithm.ALG_S1;
    } else if (isS2PreMatch(student, master)) {
      PenMatchUtils.normalizeLocalIDsFromMaster(master);
      if (isS2Match(student, master)) {
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
    if (log.isDebugEnabled()) {
      log.debug(" output :: CheckForMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: simpleCheckForMatch :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }

  /**
   * Is s 2 match boolean.
   *
   * @param student the student
   * @param master  the master
   * @return the boolean
   */
  private boolean isS2Match(PenMatchStudentDetail student, PenMasterRecord master) {
    return student.getMincode() != null
        && student.getMincode().equals(master.getMincode())
        && ((student.getLocalID() != null && master.getLocalId() != null && student.getLocalID().equals(master.getLocalId().trim()))
        || (student.getAlternateLocalID() != null && master.getAlternateLocalId() != null && student.getAlternateLocalID().equals(master.getAlternateLocalId().trim())));
  }

  /**
   * Is s 2 pre match boolean.
   *
   * @param student the student
   * @param master  the master
   * @return the boolean
   */
  private boolean isS2PreMatch(PenMatchStudentDetail student, PenMasterRecord master) {
    return student.getSurname() != null
        && student.getSurname().equals(master.getSurname().trim())
        && student.getGivenName() != null
        && student.getGivenName().equals(master.getGiven().trim())
        && student.getDob() != null
        && student.getDob().equals(master.getDob())
        && student.getLocalID() != null
        && student.getLocalID().length() > 1;
  }

  /**
   * Is s 1 match boolean.
   *
   * @param student the student
   * @param master  the master
   * @return the boolean
   */
  private boolean isS1Match(PenMatchStudentDetail student, PenMasterRecord master) {
    return student.getSurname() != null
        && student.getSurname().equals(master.getSurname().trim())
        && student.getGivenName() != null
        && student.getGivenName().equals(master.getGiven().trim())
        && student.getDob() != null
        && student.getDob().equals(master.getDob())
        && student.getSex() != null
        && student.getSex().equals(master.getSex());
  }

  /**
   * Confirm that the PEN on transaction is correct.
   *
   * @param student the student
   * @param session the session
   * @return the pen confirmation result
   */
  private PenConfirmationResult confirmPEN(PenMatchStudentDetail student, PenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    PenConfirmationResult result = new PenConfirmationResult();
    result.setPenConfirmationResultCode(PenConfirmationResult.NO_RESULT);

    String localStudentNumber = student.getPen();
    result.setDeceased(false);

    checkMatchFoundAndSetResult(student, session, result, localStudentNumber);
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenConfirmationResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: confirmPEN :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }

  /**
   * Check match found and set result.
   *
   * @param student            the student
   * @param session            the session
   * @param result             the result
   * @param localStudentNumber the local student number
   */
  private void checkMatchFoundAndSetResult(PenMatchStudentDetail student, PenMatchSession session, PenConfirmationResult result, String localStudentNumber) {
    var masterRecordOptional = lookupManager.lookupStudentByPEN(localStudentNumber);
    PenMasterRecord masterRecord = null;
    boolean matchFound = false;

    if (masterRecordOptional.isPresent()
        && StringUtils.equals(StringUtils.trimToEmpty(masterRecordOptional.get().getPen()), localStudentNumber)) {
      masterRecord = masterRecordOptional.get();
      result.setPenConfirmationResultCode(PenConfirmationResult.PEN_ON_FILE);

      String studentTrueNumber = getStudentTrueNumberForMergedStudent(masterRecord);

      if (MERGED.equals(masterRecord.getStatus()) && StringUtils.isNotBlank(studentTrueNumber)) {
        localStudentNumber = studentTrueNumber.trim();
        result.setMergedPEN(localStudentNumber);
        masterRecordOptional = lookupManager.lookupStudentByPEN(localStudentNumber);
        if (masterRecordOptional.isPresent()
            && StringUtils.equals(StringUtils.trimToEmpty(masterRecordOptional.get().getPen()), localStudentNumber)) {
          masterRecord = masterRecordOptional.get();
          matchFound = simpleCheckForMatch(student, masterRecord, session).isMatchFound();
          setDeceasedStatus(result, masterRecord);
        }
      } else {
        matchFound = simpleCheckForMatch(student, masterRecord, session).isMatchFound();
      }
      if (matchFound) {
        result.setPenConfirmationResultCode(PenConfirmationResult.PEN_CONFIRMED);
      }
    }
    result.setMasterRecord(masterRecord);
  }

  /**
   * Sets deceased status.
   *
   * @param result       the result
   * @param masterRecord the master record
   */
  private void setDeceasedStatus(PenConfirmationResult result, PenMasterRecord masterRecord) {
    if (DECEASED.equals(masterRecord.getStatus())) {
      result.setDeceased(true);
    }
  }

  /**
   * Gets student true number for merged student.
   *
   * @param masterRecord the master record
   * @return the student true number for merged student
   */
  private String getStudentTrueNumberForMergedStudent(PenMasterRecord masterRecord) {
    String studentTrueNumber = null;
    if (MERGED.equals(masterRecord.getStatus())) {
      studentTrueNumber = lookupManager.lookupStudentTruePENNumberByStudentID(masterRecord.getStudentID());
    }
    return studentTrueNumber;
  }

  /**
   * Find all possible students on master who could match the transaction - If the
   * first four characters of surname are uncommon then only use 4 characters in
   * lookup. Otherwise use 6 characters , or 5 if surname is only 5 characters
   * long use the given initial in the lookup unless 1st 4 characters of surname
   * is quite rare
   *
   * @param student          the student
   * @param penFoundOnMaster the pen found on master
   * @param session          the session
   * @param masterRecord     the master record
   */
  private void findMatchesOnPenDemog(PenMatchStudentDetail student, boolean penFoundOnMaster, PenMatchSession session, PenMasterRecord masterRecord) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMatchSession={} penFoundOnMaster={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session), penFoundOnMaster, masterRecord);
    }
    boolean type5F1 = false;

    boolean useGivenInitial = setPartials(student);

    List<StudentEntity> studentEntityList;
    if (student.getLocalID() == null) {
      if (useGivenInitial) {
        studentEntityList = lookupManager.lookupNoLocalID(student.getDob(), student.getPartialStudentSurname(), student.getPartialStudentGiven());
      } else {
        studentEntityList = lookupManager.lookupNoInitNoLocalID(student.getDob(), student.getPartialStudentSurname());
      }
    } else {
      if (useGivenInitial) {
        studentEntityList = lookupManager.lookupWithAllParts(student.getDob(), student.getPartialStudentSurname(), student.getPartialStudentGiven(), student.getMincode(), student.getLocalID());
      } else {
        studentEntityList = lookupManager.lookupNoInit(student.getDob(), student.getPartialStudentSurname(), student.getMincode(), student.getLocalID());
      }
    }

    if (masterRecord != null) {
      performCheckForMatchAndMerge(studentEntityList, student, session, masterRecord.getPen());
    } else {
      performCheckForMatchAndMerge(studentEntityList, student, session, null);
    }

    // If a PEN was provided, but the demographics didn't match the student
    // on PEN-MASTER with that PEN, then add the student on PEN-MASTER to
    // the list of possible students who match.
    if (session.getPenStatus().equals(PenStatus.B.getValue()) && penFoundOnMaster) {
      session.setReallyGoodMasterRecord(null);
      type5F1 = true;
      mergeNewMatchIntoList(student, masterRecord, masterRecord.getPen(), session, PenAlgorithm.ALG_00, 0);
    }

    // If only one really good match, and no pretty good matches,
    // just send the one PEN back
    if (session.getPenStatus().substring(0, 1).equals(PenStatus.D.getValue()) && session.getReallyGoodMasterRecord() != null && session.getPrettyGoodMatchRecord() == null) {
      session.getMatchingRecords().clear();
      session.getMatchingRecords().add(new OldPenMatchRecord(null, null, session.getReallyGoodMasterRecord().getPen(), session.getReallyGoodMasterRecord().getStudentID()));
      session.setPenStatus(PenStatus.D1.getValue());
      return;
    }

    if (session.getMatchingRecords().isEmpty()) {
      // No matches found
      session.setPenStatus(session.getPenStatus().trim() + "0");
    } else if (session.getMatchingRecords().size() == 1) {
      // 1 match only
      if (type5F1) {
        session.setPenStatus(PenStatus.F.getValue());
      } else {
        // one solid match, put in t_stud_no
        PenMatchRecord record = session.getMatchingRecords().peek();
        session.getMatchingRecords().clear();
        session.getMatchingRecords().add(new OldPenMatchRecord(null, null, record.getMatchingPEN(), record.getStudentID()));
      }
      session.setPenStatus(session.getPenStatus().trim() + "1");
    } else {
      // many matches, so they are all considered questionable, even if some are "solid"
      session.setPenStatus(session.getPenStatus().trim() + MERGED);
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: findMatchesOnPenDemog :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * Sets partials.
   *
   * @param student the student
   * @return the partials
   */
  private boolean setPartials(PenMatchStudentDetail student) {
    boolean useGivenInitial = true;
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
    return useGivenInitial;
  }

  /**
   * Merge new match into the list Assign points for algorithm and score for sort
   * use
   *
   * @param student       the student
   * @param masterRecord  the master record
   * @param matchingPEN   the matching pen
   * @param session       the session
   * @param algorithmUsed the algorithm used
   * @param totalPoints   the total points
   */
  private void mergeNewMatchIntoList(PenMatchStudentDetail student, PenMasterRecord masterRecord, String matchingPEN, PenMatchSession session, PenAlgorithm algorithmUsed, int totalPoints) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMatchSession={} matchingPEN={} PenAlgorithm={} totalPoints={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session), matchingPEN, algorithmUsed, totalPoints);
    }
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

    if (session.getMatchingRecords().size() < 20) {
      // Add new slot in the array
      session.getMatchingRecords().add(new OldPenMatchRecord(matchingAlgorithmResult, matchingScore, matchingPEN, masterRecord.getStudentID()));
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: mergeNewMatchIntoList :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

  /**
   * Check for Matching demographic data on Master
   *
   * @param student the student
   * @param master  the master
   * @param session the session
   * @return the check for match result
   */
  private CheckForMatchResult checkForMatch(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMatchSession={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session), JsonUtil.getJsonPrettyStringFromObject(master));
    }
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
      matchFound = student.getSex() == null || sexPoints != 0;
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
        session.setReallyGoodMasterRecord(master);
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
        session.setReallyGoodMasterRecord(master);
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
        session.setReallyGoodMasterRecord(master);
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
          || (bonusPoints >= 50 && student.getLocalID() != null && student.getLocalID().startsWith("ZZZ"))) {
        matchFound = true;
        algorithmUsed = PenAlgorithm.ALG_50;
        totalPoints = bonusPoints;
        if (bonusPoints >= 70) {
          session.setReallyGoodMasterRecord(master);
        } else if (bonusPoints >= 60 || localIDMatchResult.getLocalIDPoints() >= 20) {
          session.setPrettyGoodMatchRecord(master);
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
        session.setPrettyGoodMatchRecord(master);
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
    if (log.isDebugEnabled()) {
      log.debug(" output :: CheckForMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    stopwatch.stop();
    log.debug("Completed old PEN match :: checkForMatch :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
   * @param penDemogList       the pen demog list
   * @param student            the student
   * @param session            the session
   * @param localStudentNumber the local student number
   */
  private void performCheckForMatchAndMerge(List<StudentEntity> penDemogList, PenMatchStudentDetail student, PenMatchSession session, String localStudentNumber) {
    var stopwatch = Stopwatch.createStarted();
    if (log.isDebugEnabled()) {
      log.debug(" input :: penDemogList={} PenMatchStudentDetail={} PenMatchSession={} localStudentNumber={}", JsonUtil.getJsonPrettyStringFromObject(penDemogList), JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session), localStudentNumber);
    }
    if (penDemogList != null) {
      for (StudentEntity entity : penDemogList) {
        if (entity.getStatusCode() != null && !entity.getStatusCode().equals(PenStatus.M.getValue()) && !entity.getStatusCode().equals(PenStatus.D.getValue()) && (localStudentNumber == null || !entity.getPen().trim().equals(localStudentNumber))) {
          PenMasterRecord masterRecord = PenMatchUtils.convertStudentEntityToPenMasterRecord(entity);
          CheckForMatchResult result = checkForMatch(student, masterRecord, session);

          if (result.isMatchFound()) {
            String matchingPEN;
            if (result.isType5Match()) {
              matchingPEN = masterRecord.getPen().trim() + "?";
            } else {
              matchingPEN = masterRecord.getPen().trim();
            }
            mergeNewMatchIntoList(student, masterRecord, matchingPEN, session, result.getAlgorithmUsed(), result.getTotalPoints());
          }
        }
      }
    }
    stopwatch.stop();
    log.info("Completed old PEN match :: performCheckForMatchAndMerge :: in {} milli seconds", stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }

}
