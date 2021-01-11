package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.*;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.BestMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The type Pen match utils.
 */
@Slf4j
public class PenMatchUtils {
  /**
   * The constant DOB_FORMATTER_SHORT.
   */
  private static final DateTimeFormatter DOB_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyyMMdd");
  /**
   * The constant DOB_FORMATTER_LONG.
   */
  private static final DateTimeFormatter DOB_FORMATTER_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Instantiates a new Pen match utils.
   */
  private PenMatchUtils() {
  }

  /**
   * Utility method which sets the penMatchTransactionNames
   *
   * @param penMatchTransactionNames the pen match transaction names
   * @param nextNickname             the next nickname
   */
  public static void setNextNickname(PenMatchNames penMatchTransactionNames, String nextNickname) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchNames={} nextNickname={}", JsonUtil.getJsonPrettyStringFromObject(penMatchTransactionNames), nextNickname);
    }
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
   * Utility function to uppercase all incoming student data
   *
   * @param student the student
   */
  public static void upperCaseInputStudent(PenMatchStudent student) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudent={}", JsonUtil.getJsonPrettyStringFromObject(student));
    }
    student.setSurname(nullSafeTrimUpperCase(student.getSurname()));
    student.setGivenName(nullSafeTrimUpperCase(student.getGivenName()));
    student.setMiddleName(nullSafeTrimUpperCase(student.getMiddleName()));
    student.setUsualSurname(nullSafeTrimUpperCase(student.getUsualSurname()));
    student.setUsualGivenName(nullSafeTrimUpperCase(student.getUsualGivenName()));
    student.setUsualMiddleName(nullSafeTrimUpperCase(student.getUsualMiddleName()));
    student.setSex(nullSafeTrimUpperCase(student.getSex()));
    student.setPostal(nullSafeTrimUpperCase(student.getPostal()));
  }

  /**
   * Null safe trim upper case string.
   *
   * @param fieldValue the field value
   * @return the string
   */
  private static String nullSafeTrimUpperCase(String fieldValue) {
    return StringUtils.upperCase(StringUtils.trim(fieldValue));
  }

  /**
   * Converts PEN Demog record to a PEN Master record
   *
   * @param entity the entity
   * @return the pen master record
   */
  public static PenMasterRecord convertStudentEntityToPenMasterRecord(StudentEntity entity) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenDemographicsEntity={}", JsonUtil.getJsonPrettyStringFromObject(entity));
    }
    PenMasterRecord masterRecord = new PenMasterRecord();

    masterRecord.setStudentID(entity.getStudentID().toString());
    masterRecord.setPen(checkForValidValue(entity.getPen()));
    LocalDate dobDate = LocalDate.parse(entity.getDob(), DOB_FORMATTER_LONG);
    masterRecord.setDob(DOB_FORMATTER_SHORT.format(dobDate));
    masterRecord.setSurname(checkForValidValue(entity.getLegalLastName()));
    masterRecord.setGiven(checkForValidValue(entity.getLegalFirstName()));
    masterRecord.setMiddle(checkForValidValue(entity.getLegalMiddleNames()));
    masterRecord.setUsualSurname(checkForValidValue(entity.getUsualLastName()));
    masterRecord.setUsualGivenName(checkForValidValue(entity.getUsualFirstName()));
    masterRecord.setUsualMiddleName(checkForValidValue(entity.getUsualMiddleNames()));
    masterRecord.setPostal(checkForValidValue(entity.getPostalCode()));
    masterRecord.setSex(checkForValidValue(entity.getSexCode()));
    masterRecord.setGrade(checkForValidValue(entity.getGradeCode()));
    masterRecord.setStatus(checkForValidValue(entity.getStatusCode()));
    masterRecord.setMincode(checkForValidValue(entity.getMincode()));
    masterRecord.setLocalId(checkForValidValue(entity.getLocalID()));
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(masterRecord));
    }
    return masterRecord;
  }

  /**
   * Checks for valid string value
   *
   * @param value the value
   * @return the string
   */
  public static String checkForValidValue(String value) {
    if (value != null && !value.trim().isEmpty()) {
      return value.trim();
    }
    return null;
  }

  /**
   * Convert best match priority queue to list list.
   *
   * @param queue the queue
   * @return the list
   */
  public static List<PenMatchRecord> convertBestMatchPriorityQueueToList(PriorityQueue<BestMatchRecord> queue) {
    ArrayList<PenMatchRecord> matchRecords = new ArrayList<>();

    while (!queue.isEmpty()) {
      BestMatchRecord rec = queue.poll();
      matchRecords.add(new PenMatchRecord(rec.getMatchPEN(), rec.getStudentID()));
    }

    return matchRecords;
  }

  /**
   * Convert old match priority queue to list list.
   *
   * @param queue the queue
   * @return the list
   */
  public static List<PenMatchRecord> convertOldMatchPriorityQueueToList(PriorityQueue<OldPenMatchRecord> queue) {
    ArrayList<PenMatchRecord> matchRecords = new ArrayList<>();

    while (!queue.isEmpty()) {
      OldPenMatchRecord rec = queue.poll();
      matchRecords.add(new PenMatchRecord(rec.getMatchingPEN(), rec.getStudentID()));
    }

    return matchRecords;
  }

  /**
   * Check that the core data is there for a pen master add
   *
   * @param student the student
   * @param session the session
   */
  public static void checkForCoreData(PenMatchStudent student, PenMatchSession session) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudent={} PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
      session.setPenStatus(PenStatus.G0.getValue());
    }
  }

  /**
   * Check that the core data is there for a pen master add
   *
   * @param student the student
   * @param session the session
   */
  public static void checkForCoreData(PenMatchStudent student, NewPenMatchSession session) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudent={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
      session.setPenStatus(PenStatus.G0.getValue());
    }
  }

  /**
   * Strip off leading zeros , leading blanks and trailing blanks from the
   * PEN_MASTER stud_local_id. Put result in MAST_PEN_ALT_LOCAL_ID
   *
   * @param master the master
   */
  public static void normalizeLocalIDsFromMaster(PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
    }
    master.setAlternateLocalId("MMM");
    if (master.getLocalId() != null) {
      master.setAlternateLocalId(StringUtils.stripStart(master.getLocalId(), "0").replace(" ", ""));
    }
  }

  /**
   * This function stores all names in an object It includes some split logic for
   * given/middle names
   *
   * @param master the master
   * @return the pen match names
   */
  public static PenMatchNames storeNamesFromMaster(PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
    }
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
    if (log.isDebugEnabled()) {
      log.debug(" output :: PenMatchNames={}", JsonUtil.getJsonPrettyStringFromObject(penMatchMasterNames));
    }
    return penMatchMasterNames;
  }

  /**
   * Small utility method for storing names to keep things clean
   *
   * @param name the name
   * @return the string
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
   * @param pen the pen
   * @return the boolean
   */
  public static boolean penCheckDigit(String pen) {
    log.debug(" input :: pen={}", pen);
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

    StringBuilder fullEvenStringBuilder = new StringBuilder();
    for (int i : evens) {
      fullEvenStringBuilder.append(i);
    }

    ArrayList<Integer> listOfFullEvenValueDoubled = new ArrayList<>();
    String fullEvenValueDoubledString = Integer.toString(Integer.parseInt(fullEvenStringBuilder.toString()) * 2);
    for (int i = 0; i < fullEvenValueDoubledString.length(); i++) {
      listOfFullEvenValueDoubled.add(Integer.parseInt(fullEvenValueDoubledString.substring(i, i + 1)));
    }

    int sumEvens = listOfFullEvenValueDoubled.stream().mapToInt(Integer::intValue).sum();

    int finalSum = sumEvens + sumOdds;

    String penCheckDigit = pen.substring(8, 9);


    boolean result = ((finalSum % 10 == 0 && penCheckDigit.equals("0")) || ((10 - finalSum % 10) == Integer.parseInt(penCheckDigit)));
    log.debug(" output :: booleanResult={}", result);
    return result;
  }

  /**
   * Utility method which will drop spaces, dashes & apostrophes
   *
   * @param name the name
   * @return the string
   */
  public static String dropNonLetters(String name) {
    return StringUtils.replaceEach(name, new String[]{" ", "-", "'"}, new String[]{"", "", ""});
  }

  /**
   * Replaces hyphens with spaces
   *
   * @param name the name
   * @return the string
   */
  public static String replaceHyphensWithBlank(String name) {
    return StringUtils.replace(name, "-", " ");
  }

  /**
   * Small utility method to check for partial name
   *
   * @param transactionName the transaction name
   * @param masterName      the master name
   * @return the boolean
   */
  public static boolean checkForPartialName(String transactionName, String masterName) {
    return (transactionName.contains(masterName) || masterName.contains(transactionName));
  }
}
