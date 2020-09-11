package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

@Slf4j
public class PenMatchUtils {

    private PenMatchUtils() {
    }

    /**
     * Utility method which sets the penMatchTransactionNames
     */
    public static void setNextNickname(PenMatchNames penMatchTransactionNames, String nextNickname) {
        log.info(" input :: PenMatchNames={} nextNickname={}", JsonUtil.getJsonPrettyStringFromObject(penMatchTransactionNames), nextNickname);
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
     */
    public static void upperCaseInputStudent(PenMatchStudent student) {
        log.info(" input :: PenMatchStudent={}", JsonUtil.getJsonPrettyStringFromObject(student));
        if (student.getSurname() != null) {
            student.setSurname(student.getSurname().trim().toUpperCase());
        }

        if (student.getGivenName() != null) {
            student.setGivenName(student.getGivenName().trim().toUpperCase());
        }

        if (student.getMiddleName() != null) {
            student.setMiddleName(student.getMiddleName().trim().toUpperCase());
        }

        if (student.getUsualSurname() != null) {
            student.setUsualSurname(student.getUsualSurname().trim().toUpperCase());
        }

        if (student.getUsualGivenName() != null) {
            student.setUsualGivenName(student.getUsualGivenName().trim().toUpperCase());
        }

        if (student.getUsualMiddleName() != null) {
            student.setUsualMiddleName(student.getUsualMiddleName().trim().toUpperCase());
        }

        if (student.getSex() != null) {
            student.setSex(student.getSex().trim().toUpperCase());
        }

        if (student.getPostal() != null) {
            student.setPostal(student.getPostal().trim().toUpperCase());
        }

        if (student.getGivenInitial() != null) {
            student.setGivenInitial(student.getGivenInitial().trim().toUpperCase());
        }

        if (student.getMiddleInitial() != null) {
            student.setMiddleInitial(student.getMiddleInitial().trim().toUpperCase());
        }

    }

    /**
     * Converts PEN Demog record to a PEN Master record
     */
    public static PenMasterRecord convertStudentEntityToPenMasterRecord(StudentEntity entity) {
        log.info(" input :: PenDemographicsEntity={}", JsonUtil.getJsonPrettyStringFromObject(entity));
        PenMasterRecord masterRecord = new PenMasterRecord();

        masterRecord.setStudentID(entity.getStudentID());
        masterRecord.setPen(checkForValidValue(entity.getPen()));
        masterRecord.setDob(checkForValidValue(entity.getDob()));
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

        log.info(" output :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(masterRecord));
        return masterRecord;
    }

    /**
     * Checks for valid string value
     */
    public static String checkForValidValue(String value) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return null;
    }

    /**
     * Check that the core data is there for a pen master add
     */
    public static void checkForCoreData(PenMatchStudent student, PenMatchSession session) {
        log.info(" input :: PenMatchStudent={} PenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
        if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
            session.setPenStatus(PenStatus.G0.getValue());
        }
    }

    /**
     * Check that the core data is there for a pen master add
     */
    public static void checkForCoreData(PenMatchStudent student, NewPenMatchSession session) {
        log.info(" input :: PenMatchStudent={} NewPenMatchSession={}", JsonUtil.getJsonPrettyStringFromObject(student), JsonUtil.getJsonPrettyStringFromObject(session));
        if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null || student.getSex() == null || student.getMincode() == null) {
            session.setPenStatus(PenStatus.G0.getValue());
        }
    }

    /**
     * Strip off leading zeros , leading blanks and trailing blanks from the
     * PEN_MASTER stud_local_id. Put result in MAST_PEN_ALT_LOCAL_ID
     */
    public static void normalizeLocalIDsFromMaster(PenMasterRecord master) {
        log.info(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
        master.setAlternateLocalId("MMM");
        if (master.getLocalId() != null) {
            master.setAlternateLocalId(StringUtils.stripStart(master.getLocalId(), "0").replace(" ", ""));
        }
    }

    /**
     * This function stores all names in an object It includes some split logic for
     * given/middle names
     */
    public static PenMatchNames storeNamesFromMaster(PenMasterRecord master) {
        log.info(" input :: PenMasterRecord={}", JsonUtil.getJsonPrettyStringFromObject(master));
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
        log.info(" output :: PenMatchNames={}", JsonUtil.getJsonPrettyStringFromObject(penMatchMasterNames));
        return penMatchMasterNames;
    }

    /**
     * Small utility method for storing names to keep things clean
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
     */
    public static boolean penCheckDigit(String pen) {
        log.info(" input :: pen={}", pen);
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
        log.info(" output :: booleanResult={}", result);
        return result;
    }

    /**
     * Utility method which will drop spaces, dashes & apostrophes
     */
    public static String dropNonLetters(String name) {
        if(name != null){
            return name.replace(" ", "").replace("-", "").replace("'","");
        }
        return null;
    }

    /**
     * Replaces hyphens with spaces
     */
    public static String replaceHyphensWithBlank(String name) {
        if(name != null){
            return name.replace("-", " ");
        }
        return null;
    }

    /**
     * Small utility method to check for partial name
     *
     * @return
     */
    public static boolean checkForPartialName(String transactionName, String masterName) {
        boolean partialName = false;
        if (transactionName.contains(masterName) || masterName.contains(transactionName)) {
            return true;
        }

        return false;
    }
}
