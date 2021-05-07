package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.struct.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;

/**
 * The type Scoring utils.
 */
@Slf4j
public class ScoringUtils {

  /**
   * Instantiates a new Scoring utils.
   */
  private ScoringUtils() {
  }

  /**
   * Calculate points for Birthday match
   *
   * @param student the student
   * @param master  the master
   * @return the int
   */
  public static int matchBirthday(PenMatchStudentDetail student, PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master));
    }
    int birthdayPoints = 0;
    int birthdayMatches = 0;
    String dob = student.getDob();

    String masterDob = master.getDob();
    if (dob == null) {
      return 0;
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
      if (dob.substring(0, 4).equals(masterDob.substring(0, 4)) && dob.substring(4, 6).equals(masterDob.substring(6, 8)) && dob.substring(6, 8).equals(masterDob.substring(4, 6))) {
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
            if (dob.substring(0, 4).equals(masterDob.substring(0, 4)) && dob.substring(6, 8).equals(masterDob.substring(6, 8))) {
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

    log.debug(" output :: birthdayPoints={}", birthdayPoints);
    return birthdayPoints;
  }

  /**
   * Calculate points for Local ID/School code combination - Some schools misuse
   * the local ID field with a bogus 1 character local ID unfortunately this will
   * jeopardize the checking for legit 1 character local IDs
   *
   * @param student the student
   * @param master  the master
   * @param session the session
   * @return the local id match result
   */
  public static LocalIDMatchResult matchLocalID(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={} PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master), JsonUtil.getJsonPrettyStringFromObject(session));
    }
    LocalIDMatchResult matchResult = new LocalIDMatchResult();
    int localIDPoints = 0;
    String mincode = student.getMincode();
    String localID = student.getLocalID();
    String masterMincode = master.getMincode();
    String masterLocalID = master.getLocalId();

    if (mincode != null && mincode.equals(masterMincode) && ((localID != null && localID.equals(masterLocalID)) || (student.getAlternateLocalID() != null && student.getAlternateLocalID().equals(master.getAlternateLocalId())))
        && (localID != null && localID.trim().length() > 1)) {
      localIDPoints = 20;
    }

    // Same School
    if (localIDPoints == 0 && mincode != null && mincode.equals(masterMincode)) {
      localIDPoints = 10;
    }

    // Same district
    if (localIDPoints == 0 && mincode != null && masterMincode != null && mincode.substring(0, 3).equals(masterMincode.substring(0, 3)) && !mincode.startsWith("102")) {
      localIDPoints = 5;
    }

    // Prepare to negate any local_id_points if the local ids actually conflict
    if (localIDPoints > 0 && mincode.equals(masterMincode)) {
      if(StringUtils.isNotBlank(localID) && StringUtils.isNotBlank(masterLocalID) && !localID.equals(masterLocalID)){
        //if ((localID == null && masterLocalID != null) || (localID != null && masterLocalID == null) || (localID != null && !localID.equals(masterLocalID))) {
        if ((StringUtils.isNotBlank(student.getAlternateLocalID()) && StringUtils.isNotBlank(master.getAlternateLocalId()) && !student.getAlternateLocalID().equals(master.getAlternateLocalId())) || (StringUtils.isBlank(student.getAlternateLocalID()) && StringUtils.isBlank(master.getAlternateLocalId()))) {
          matchResult.setIdDemerits(localIDPoints);
        }
      }
    }

    matchResult.setLocalIDPoints(localIDPoints);
    if (log.isDebugEnabled()) {
      log.debug(" output :: LocalIDMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(matchResult));
    }
    return matchResult;
  }

  /**
   * Calculate points for address match
   *
   * @param student the student
   * @param master  the master
   * @return the int
   */
  public static int matchAddress(PenMatchStudentDetail student, PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master));
    }
    int addressPoints = 0;
    String postal = student.getPostal();
    String masterPostal = master.getPostal();

    if (postal != null && postal.equals(masterPostal) && !masterPostal.startsWith("V0")) {
      addressPoints = 10;
    } else if (postal != null && postal.equals(masterPostal) && masterPostal.startsWith("V0")) {
      addressPoints = 1;
    }
    log.debug(" output :: addressPoints={}", addressPoints);
    return addressPoints;
  }

  /**
   * Calculate points for surname match
   *
   * @param student the student
   * @param master  the master
   * @return the surname match result
   */
  public static SurnameMatchResult matchSurname(PenMatchStudentDetail student, PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master));
    }
    int surnamePoints = 0;
    boolean legalSurnameUsed = false;
    String masterLegalSurnameNoBlanks = null;
    String masterUsualSurnameNoBlanks = null;
    String studentSurnameNoBlanks = null;
    String usualSurnameNoBlanks = null;

    if (master.getSurname() != null) {
      masterLegalSurnameNoBlanks = master.getSurname().replace(" ", "");
    }
    if (master.getUsualSurname() != null) {
      masterUsualSurnameNoBlanks = master.getUsualSurname().replace(" ", "");
    }
    if (student.getSurname() != null) {
      studentSurnameNoBlanks = student.getSurname().replace(" ", "");
    }
    if (student.getUsualSurname() != null) {
      usualSurnameNoBlanks = student.getUsualSurname().replace(" ", "");
    }

    if (studentSurnameNoBlanks != null && studentSurnameNoBlanks.equals(masterLegalSurnameNoBlanks)) {
      // Verify if legal surname matches master legal surname
      surnamePoints = 20;
      legalSurnameUsed = true;
    } else if (usualSurnameNoBlanks != null && usualSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) {
      // Verify is usual surname matches master usual surname
      surnamePoints = 20;
    } else if (studentSurnameNoBlanks != null && studentSurnameNoBlanks.equals(masterUsualSurnameNoBlanks)) {
      // Verify if legal surname matches master usual surname
      surnamePoints = 20;
      legalSurnameUsed = true;
    } else if (usualSurnameNoBlanks != null && usualSurnameNoBlanks.equals(masterLegalSurnameNoBlanks)) {
      // Verify if usual surname matches master legal surname
      surnamePoints = 20;
    } else if (studentSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null && studentSurnameNoBlanks.length() >= 4 && masterLegalSurnameNoBlanks.length() >= 4 && studentSurnameNoBlanks.substring(0, 4).equals(masterLegalSurnameNoBlanks.substring(0, 4))) {
      // Do a 4 character match with legal surname and master legal surname
      surnamePoints = 10;
    } else if (usualSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null && usualSurnameNoBlanks.length() >= 4 && masterUsualSurnameNoBlanks.length() >= 4 && usualSurnameNoBlanks.substring(0, 4).equals(masterUsualSurnameNoBlanks.substring(0, 4))) {
      // Do a 4 character match with usual surname and master usual surname
      surnamePoints = 10;
    } else if (studentSurnameNoBlanks != null && masterUsualSurnameNoBlanks != null && studentSurnameNoBlanks.length() >= 4 && masterUsualSurnameNoBlanks.length() >= 4 && studentSurnameNoBlanks.substring(0, 4).equals(masterUsualSurnameNoBlanks.substring(0, 4))) {
      // Do a 4 character match with legal surname and master usual surname
      surnamePoints = 10;
    } else if (usualSurnameNoBlanks != null && masterLegalSurnameNoBlanks != null && usualSurnameNoBlanks.length() >= 4 && masterLegalSurnameNoBlanks.length() >= 4 && usualSurnameNoBlanks.substring(0, 4).equals(masterLegalSurnameNoBlanks.substring(0, 4))) {
      // Do a 4 character match with usual surname and master legal surname
      surnamePoints = 10;
    } else {
      String masterSoundexLegalSurname = runSoundex(masterLegalSurnameNoBlanks);
      String masterSoundexUsualSurname = runSoundex(masterUsualSurnameNoBlanks);

      String soundexLegalSurname = runSoundex(studentSurnameNoBlanks);
      String soundexUsualSurname = runSoundex(usualSurnameNoBlanks);

      if (soundexLegalSurname != null && soundexLegalSurname.length() > 0 && soundexLegalSurname.charAt(0) != ' ' && soundexLegalSurname.equals(masterSoundexLegalSurname)) {
        // Check if the legal surname soundex matches the master legal surname soundex
        surnamePoints = 10;
      } else if (soundexUsualSurname != null && soundexUsualSurname.length() > 0 && soundexUsualSurname.charAt(0) != ' ' && soundexUsualSurname.equals(masterSoundexLegalSurname)) {
        // Check if the usual surname soundex matches the master legal surname soundex
        surnamePoints = 10;
      } else if (soundexLegalSurname != null && soundexLegalSurname.length() > 0 && soundexLegalSurname.charAt(0) != ' ' && soundexLegalSurname.equals(masterSoundexUsualSurname)) {
        // Check if the legal surname soundex matches the master usual surname soundex
        surnamePoints = 10;
      } else if (soundexUsualSurname != null && soundexUsualSurname.length() > 0 && soundexUsualSurname.charAt(0) != ' ' && soundexUsualSurname.equals(masterSoundexUsualSurname)) {
        // Check if the usual surname soundex matches the master usual surname soundex
        surnamePoints = 10;
      }
    }

    SurnameMatchResult result = new SurnameMatchResult();
    result.setLegalSurnameUsed(legalSurnameUsed);
    result.setSurnamePoints(surnamePoints);
    if (log.isDebugEnabled()) {
      log.debug(" output :: SurnameMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    return result;
  }

  /**
   * Calculate points for given name match
   *
   * @param penMatchTransactionNames the pen match transaction names
   * @param penMatchMasterNames      the pen match master names
   * @return the given name match result
   */
  public static GivenNameMatchResult matchGivenName(PenMatchNames penMatchTransactionNames, PenMatchNames penMatchMasterNames) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: penMatchTransactionNames={} penMatchMasterNames={}", JsonUtil.getJsonPrettyStringFromObject(penMatchTransactionNames), JsonUtil.getJsonPrettyStringFromObject(penMatchMasterNames));
    }
    int givenNamePoints = 0;
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

    if ((hasGivenNameFullCharMatch(legalGiven, penMatchMasterNames)) || (hasGivenNameFullCharMatch(usualGiven, penMatchMasterNames)) || (hasGivenNameFullCharMatch(alternateLegalGiven, penMatchMasterNames)) || (hasGivenNameFullCharMatch(alternateUsualGiven, penMatchMasterNames))) {
      // 10 Character match
      givenNamePoints = 20;
    } else if ((hasGivenNameSubsetCharMatch(legalGiven, 10, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(usualGiven, 10, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(alternateLegalGiven, 10, penMatchMasterNames))
        || (hasGivenNameSubsetCharMatch(alternateUsualGiven, 10, penMatchMasterNames))) {
      // 10 Character match
      givenNamePoints = 20;
    } else if ((hasGivenNameSubsetMatch(legalGiven, penMatchMasterNames)) || (hasGivenNameSubsetMatch(usualGiven, penMatchMasterNames)) || (hasGivenNameSubsetMatch(alternateLegalGiven, penMatchMasterNames)) || (hasGivenNameSubsetMatch(alternateUsualGiven, penMatchMasterNames))) {
      // Has a subset match
      givenNamePoints = 15;
    } else if ((hasGivenNameSubsetCharMatch(legalGiven, 4, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(usualGiven, 4, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(alternateLegalGiven, 4, penMatchMasterNames))
        || (hasGivenNameSubsetCharMatch(alternateUsualGiven, 4, penMatchMasterNames))) {
      // 4 Character Match
      givenNamePoints = 15;
    } else if ((hasGivenNameSubsetCharMatch(nickname1, 10, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(nickname2, 10, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(nickname3, 10, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(nickname4, 10, penMatchMasterNames))) {
      // No 4 character matches found , try nicknames
      givenNamePoints = 10;
    } else if ((hasGivenNameSubsetCharMatch(legalGiven, 1, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(usualGiven, 1, penMatchMasterNames)) || (hasGivenNameSubsetCharMatch(alternateLegalGiven, 1, penMatchMasterNames))
        || (hasGivenNameSubsetCharMatch(alternateUsualGiven, 1, penMatchMasterNames))) {
      // 1 Character Match
      givenNamePoints = 5;
    } else if ((hasGivenNameSubsetToMiddleNameMatch(legalGiven, penMatchMasterNames)) || (hasGivenNameSubsetToMiddleNameMatch(usualGiven, penMatchMasterNames)) || (hasGivenNameSubsetToMiddleNameMatch(alternateLegalGiven, penMatchMasterNames))
        || (hasGivenNameSubsetToMiddleNameMatch(alternateUsualGiven, penMatchMasterNames))) {
      // Check Given to Middle if no matches above (only try 4 characters)
      givenNamePoints = 10;
      givenFlip = true;
    }

    GivenNameMatchResult result = new GivenNameMatchResult();
    result.setGivenNamePoints(givenNamePoints);
    result.setGivenNameFlip(givenFlip);
    if (log.isDebugEnabled()) {
      log.debug(" output :: GivenNameMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    return result;
  }

  /**
   * Calculate points for middle name match
   *
   * @param penMatchTransactionNames the pen match transaction names
   * @param penMatchMasterNames      the pen match master names
   * @return the middle name match result
   */
  public static MiddleNameMatchResult matchMiddleName(PenMatchNames penMatchTransactionNames, PenMatchNames penMatchMasterNames) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: penMatchTransactionNames={} penMatchMasterNames={}", JsonUtil.getJsonPrettyStringFromObject(penMatchTransactionNames), JsonUtil.getJsonPrettyStringFromObject(penMatchMasterNames));
    }
    int middleNamePoints = 0;
    boolean middleFlip = false;

    String legalMiddle = penMatchTransactionNames.getLegalMiddle();
    String usualMiddle = penMatchTransactionNames.getUsualMiddle();
    String alternateLegalMiddle = penMatchTransactionNames.getAlternateLegalMiddle();
    String alternateUsualMiddle = penMatchTransactionNames.getAlternateUsualMiddle();

    if ((hasMiddleNameFullCharMatch(legalMiddle, penMatchMasterNames)) || (hasMiddleNameFullCharMatch(usualMiddle, penMatchMasterNames)) || (hasMiddleNameFullCharMatch(alternateLegalMiddle, penMatchMasterNames)) || (hasMiddleNameFullCharMatch(alternateUsualMiddle, penMatchMasterNames))) {
      // Full Match
      middleNamePoints = 20;
    } else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 10, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(usualMiddle, 10, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 10, penMatchMasterNames))
        || (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 10, penMatchMasterNames))) {
      // 10 Character match
      middleNamePoints = 20;
    } else if ((hasMiddleNameSubsetMatch(legalMiddle, penMatchMasterNames)) || (hasMiddleNameSubsetMatch(usualMiddle, penMatchMasterNames)) || (hasMiddleNameSubsetMatch(alternateLegalMiddle, penMatchMasterNames)) || (hasMiddleNameSubsetMatch(alternateUsualMiddle, penMatchMasterNames))) {
      // Has a subset match
      middleNamePoints = 15;
    } else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 4, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(usualMiddle, 4, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 4, penMatchMasterNames))
        || (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 4, penMatchMasterNames))) {
      // 4 Character Match
      middleNamePoints = 15;
    } else if ((hasMiddleNameSubsetCharMatch(legalMiddle, 1, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(usualMiddle, 1, penMatchMasterNames)) || (hasMiddleNameSubsetCharMatch(alternateLegalMiddle, 1, penMatchMasterNames))
        || (hasMiddleNameSubsetCharMatch(alternateUsualMiddle, 1, penMatchMasterNames))) {
      // 1 Character Match
      middleNamePoints = 5;
    } else if ((hasMiddleNameSubsetToGivenNameMatch(legalMiddle, penMatchMasterNames)) || (hasMiddleNameSubsetToGivenNameMatch(usualMiddle, penMatchMasterNames)) || (hasMiddleNameSubsetToGivenNameMatch(alternateLegalMiddle, penMatchMasterNames))
        || (hasMiddleNameSubsetToGivenNameMatch(alternateUsualMiddle, penMatchMasterNames))) {
      // Check middle to given if no matches above (only try 4 characters)
      middleNamePoints = 10;
      middleFlip = true;
    }

    MiddleNameMatchResult result = new MiddleNameMatchResult();
    result.setMiddleNamePoints(middleNamePoints);
    result.setMiddleNameFlip(middleFlip);
    if (log.isDebugEnabled()) {
      log.debug(" output :: MiddleNameMatchResult={}", JsonUtil.getJsonPrettyStringFromObject(result));
    }
    return result;
  }

  /**
   * Soundex calculation
   *
   * @param inputString the input string
   * @return the string
   */
  private static String runSoundex(String inputString) {
    log.debug(" input :: soundexInputString={}", inputString);

    String previousCharRaw;
    int previousCharSoundex;
    String currentCharRaw;
    int currentCharSoundex;
    String soundexString;
    String tempString;

    if (inputString != null && inputString.length() >= 2) {
      Soundex soundex = new Soundex();
      tempString = soundex.soundex(inputString);
      soundexString = inputString.substring(0, 1);
      previousCharRaw = inputString.substring(0, 1);
      previousCharSoundex = -1;
      currentCharRaw = inputString.substring(1, 2);

      StringBuilder soundexStringBuilder = new StringBuilder();
      soundexStringBuilder.append(soundexString);
      for (int i = 1; i < tempString.length(); i++) {
        currentCharSoundex = Integer.parseInt(tempString.substring(i, i + 1));

        if (currentCharSoundex >= 1 && currentCharSoundex <= 7) {
          // If the second "soundexable" character is not the same as the first raw
          // character then append the soundex value of this character to the soundex
          // string. If this is the third or greater soundexable value, then if the
          // soundex
          // value of the character is not equal to the soundex value of the previous
          // character, then append that soundex value to the soundex string.
          if (i == 1) {
            if (!currentCharRaw.equals(previousCharRaw)) {
              soundexStringBuilder.append(currentCharSoundex);
              previousCharSoundex = currentCharSoundex;
            }
          } else if (currentCharSoundex != previousCharSoundex) {
            soundexStringBuilder.append(currentCharSoundex);
            previousCharSoundex = currentCharSoundex;
          }
        }
      }

      soundexString = (soundexStringBuilder.append("00000000")).substring(0, 8);
    } else {
      return null;
    }
    log.debug(" output :: soundexString={}", soundexString);
    return soundexString;
  }

  /**
   * Utility function to check for subset given name matches
   *
   * @param givenName           the given name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasGivenNameSubsetMatch(String givenName, PenMatchNames penMatchMasterNames) {
    if (givenName != null && givenName.length() >= 1) {
      return (penMatchMasterNames.getLegalGiven() != null && (penMatchMasterNames.getLegalGiven().contains(givenName) || givenName.contains(penMatchMasterNames.getLegalGiven())))
          || (penMatchMasterNames.getUsualGiven() != null && (penMatchMasterNames.getUsualGiven().contains(givenName) || givenName.contains(penMatchMasterNames.getUsualGiven())))
          || (penMatchMasterNames.getAlternateLegalGiven() != null && (penMatchMasterNames.getAlternateLegalGiven().contains(givenName) || givenName.contains(penMatchMasterNames.getAlternateLegalGiven())))
          || (penMatchMasterNames.getAlternateUsualGiven() != null && (penMatchMasterNames.getAlternateUsualGiven().contains(givenName) || givenName.contains(penMatchMasterNames.getAlternateUsualGiven())));
    }
    return false;
  }

  /**
   * Utility function for subset match
   *
   * @param givenName           the given name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasGivenNameFullCharMatch(String givenName, PenMatchNames penMatchMasterNames) {
    if (givenName != null) {
      return (penMatchMasterNames.getLegalGiven() != null && penMatchMasterNames.getLegalGiven().equals(givenName)) || (penMatchMasterNames.getUsualGiven() != null && penMatchMasterNames.getUsualGiven().equals(givenName))
          || (penMatchMasterNames.getAlternateLegalGiven() != null && penMatchMasterNames.getAlternateLegalGiven().equals(givenName)) || (penMatchMasterNames.getAlternateUsualGiven() != null && penMatchMasterNames.getAlternateUsualGiven().equals(givenName));
    }
    return false;
  }

  /**
   * Utility function for subset match
   *
   * @param givenName           the given name
   * @param numOfChars          the num of chars
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasGivenNameSubsetCharMatch(String givenName, int numOfChars, PenMatchNames penMatchMasterNames) {
    if (givenName != null && givenName.length() >= numOfChars) {
      return (penMatchMasterNames.getLegalGiven() != null && penMatchMasterNames.getLegalGiven().length() >= numOfChars && penMatchMasterNames.getLegalGiven().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getUsualGiven() != null && penMatchMasterNames.getUsualGiven().length() >= numOfChars && penMatchMasterNames.getUsualGiven().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateLegalGiven() != null && penMatchMasterNames.getAlternateLegalGiven().length() >= numOfChars && penMatchMasterNames.getAlternateLegalGiven().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateUsualGiven() != null && penMatchMasterNames.getAlternateUsualGiven().length() >= numOfChars && penMatchMasterNames.getAlternateUsualGiven().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)));
    }
    return false;
  }

  /**
   * Utility function to check for subset given name matches to given names
   *
   * @param givenName           the given name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasGivenNameSubsetToMiddleNameMatch(String givenName, PenMatchNames penMatchMasterNames) {
    int numOfChars = 4;
    if (givenName != null && givenName.length() >= numOfChars) {
      return (penMatchMasterNames.getLegalMiddle() != null && penMatchMasterNames.getLegalMiddle().length() >= numOfChars && penMatchMasterNames.getLegalMiddle().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getUsualMiddle() != null && penMatchMasterNames.getUsualMiddle().length() >= numOfChars && penMatchMasterNames.getUsualMiddle().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateLegalMiddle() != null && penMatchMasterNames.getAlternateLegalMiddle().length() >= numOfChars && penMatchMasterNames.getAlternateLegalMiddle().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateUsualMiddle() != null && penMatchMasterNames.getAlternateUsualMiddle().length() >= numOfChars && penMatchMasterNames.getAlternateUsualMiddle().substring(0, numOfChars).equals(givenName.substring(0, numOfChars)));
    }
    return false;
  }

  /**
   * Utility function to check for subset middle name matches
   *
   * @param middleName          the middle name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasMiddleNameSubsetMatch(String middleName, PenMatchNames penMatchMasterNames) {
    if (middleName != null && middleName.length() > 1) {
      return (penMatchMasterNames.getLegalMiddle() != null && (penMatchMasterNames.getLegalMiddle().contains(middleName) || middleName.contains(penMatchMasterNames.getLegalMiddle())))
          || (penMatchMasterNames.getUsualMiddle() != null && (penMatchMasterNames.getUsualMiddle().contains(middleName) || middleName.contains(penMatchMasterNames.getUsualMiddle())))
          || (penMatchMasterNames.getAlternateLegalMiddle() != null && (penMatchMasterNames.getAlternateLegalMiddle().contains(middleName) || middleName.contains(penMatchMasterNames.getAlternateLegalMiddle())))
          || (penMatchMasterNames.getAlternateUsualMiddle() != null && (penMatchMasterNames.getAlternateUsualMiddle().contains(middleName) || middleName.contains(penMatchMasterNames.getAlternateUsualMiddle())));
    }
    return false;
  }

  /**
   * Utility function for subset match
   *
   * @param middleName          the middle name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasMiddleNameFullCharMatch(String middleName, PenMatchNames penMatchMasterNames) {
    if (middleName != null) {
      return (penMatchMasterNames.getLegalMiddle() != null && penMatchMasterNames.getLegalMiddle().equals(middleName)) || (penMatchMasterNames.getUsualMiddle() != null && penMatchMasterNames.getUsualMiddle().equals(middleName))
          || (penMatchMasterNames.getAlternateLegalMiddle() != null && penMatchMasterNames.getAlternateLegalMiddle().equals(middleName)) || (penMatchMasterNames.getAlternateUsualMiddle() != null && penMatchMasterNames.getAlternateUsualMiddle().equals(middleName));
    }
    return false;
  }

  /**
   * Utility function for subset match
   *
   * @param middleName          the middle name
   * @param numOfChars          the num of chars
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasMiddleNameSubsetCharMatch(String middleName, int numOfChars, PenMatchNames penMatchMasterNames) {
    if (middleName != null && middleName.length() >= numOfChars) {
      return (penMatchMasterNames.getLegalMiddle() != null && penMatchMasterNames.getLegalMiddle().length() >= numOfChars && penMatchMasterNames.getLegalMiddle().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getUsualMiddle() != null && penMatchMasterNames.getUsualMiddle().length() >= numOfChars && penMatchMasterNames.getUsualMiddle().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateLegalMiddle() != null && penMatchMasterNames.getAlternateLegalMiddle().length() >= numOfChars && penMatchMasterNames.getAlternateLegalMiddle().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateUsualMiddle() != null && penMatchMasterNames.getAlternateUsualMiddle().length() >= numOfChars && penMatchMasterNames.getAlternateUsualMiddle().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)));
    }
    return false;
  }

  /**
   * Utility function to check for subset middle name matches to given names
   *
   * @param middleName          the middle name
   * @param penMatchMasterNames the pen match master names
   * @return the boolean
   */
  public static boolean hasMiddleNameSubsetToGivenNameMatch(String middleName, PenMatchNames penMatchMasterNames) {
    int numOfChars = 4;
    if (middleName != null && middleName.length() >= numOfChars) {
      return (penMatchMasterNames.getLegalGiven() != null && penMatchMasterNames.getLegalGiven().length() >= numOfChars && penMatchMasterNames.getLegalGiven().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getUsualGiven() != null && penMatchMasterNames.getUsualGiven().length() >= numOfChars && penMatchMasterNames.getUsualGiven().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateLegalGiven() != null && penMatchMasterNames.getAlternateLegalGiven().length() >= numOfChars && penMatchMasterNames.getAlternateLegalGiven().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)))
          || (penMatchMasterNames.getAlternateUsualGiven() != null && penMatchMasterNames.getAlternateUsualGiven().length() >= numOfChars && penMatchMasterNames.getAlternateUsualGiven().substring(0, numOfChars).equals(middleName.substring(0, numOfChars)));
    }
    return false;
  }

  /**
   * Calculate points for Sex match
   *
   * @param student the student
   * @param master  the master
   * @return the int
   */
  public static int matchSex(PenMatchStudentDetail student, PenMasterRecord master) {
    if (log.isDebugEnabled()) {
      log.debug(" input :: PenMatchStudentDetail={} PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(master));
    }
    int sexPoints = 0;
    if (student.getSex() != null && master.getSex() != null && student.getSex().equals(master.getSex().trim())) {
      sexPoints = 5;
    }
    log.debug(" output :: sexPoints={}", sexPoints);
    return sexPoints;
  }
}
