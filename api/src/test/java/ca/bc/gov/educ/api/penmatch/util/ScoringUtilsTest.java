package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.struct.v1.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ScoringUtilsTest {
  @ClassRule
  public static final SpringClassRule scr = new SpringClassRule();
  /**
   * The Smr.
   */
  @Rule
  public final SpringMethodRule smr = new SpringMethodRule();

  @Before
  public void before() {
  }

  @Test
  @Parameters({
      ",,0",
      "V0R3W5,V0R3W5,1",
      "V1R3W5,V1R3W5,10",
      "V8W2E1,V8W2E1,10",
      "V8R4N4,V8R4N4,10",
  })
  public void testMatchAddress_givenDifferentInputs_shouldReturnExpectedScore(String studentPostal, String masterPostal, int expectedPoints) {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    if (StringUtils.isNotBlank(studentPostal)) {
      student.setPostal(studentPostal);
    }
    if (StringUtils.isNotBlank(masterPostal)) {
      master.setPostal(masterPostal);
    }
    assertEquals(expectedPoints, ScoringUtils.matchAddress(student, master));
  }


  @Test
  @Parameters({
      "null,null,0",
      "19800518,19800518,20",
      "19801805,19800518,15",
      "20100518,19800518,15",
      "19800510,19800518,10",
      "19801018,19800518,10",
      "20010518,19800518,5",
      "19801018,19800519,5",
  })
  public void testMatchBirthday_givenDifferentInputs_shouldReturnExpectedResults(String studentBirthday, String masterBirthday, int expectedScore) {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    if (StringUtils.isNotBlank(studentBirthday)) {
      student.setDob("null".equalsIgnoreCase(studentBirthday) ? null : studentBirthday);
    }
    if (StringUtils.isNotBlank(masterBirthday)) {
      master.setDob("null".equalsIgnoreCase(masterBirthday) ? null : masterBirthday);
    }
    assertEquals(expectedScore, ScoringUtils.matchBirthday(student, master));
  }

  @Test
  @Parameters(method = "testMatchLocalID_AlternateLocalIdScore20," +
      "testMatchLocalID_ShouldScore20," +
      "testMatchLocalIDSameSchool_ShouldScore10," +
      "testMatchLocalIDSameDistrict_ShouldScore5")
  public void testMatchLocalID_givenDifferentInputs_shouldReturnExpectedResults(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session, int expectedScore) {
    assertEquals(expectedScore, ScoringUtils.matchLocalID(student, master, session).getLocalIDPoints());
  }


  @Test
  @Parameters(method = "testMatchLocalIDWithDemerits_ShouldScore10Demerits," +
      "testMatchLocalIDWithDemerits_ShouldScore10DemeritsWithAlternate")
  public void testMatchLocalIDWithDemerits_givenDifferentInputs_shouldReturnExpectedResults(PenMatchStudentDetail student, PenMasterRecord master, PenMatchSession session, int expectedLocalId, int expectedDemerits) {

    LocalIDMatchResult result = ScoringUtils.matchLocalID(student, master, session);
    assertEquals(expectedDemerits, result.getIdDemerits());
    assertEquals(expectedLocalId, result.getLocalIDPoints());
  }

  @Test
  public void testMatchSex_ShouldScore5() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSex("M");
    master.setSex("M");

    assertEquals(5, ScoringUtils.matchSex(student, master));
  }

  @Test
  @Parameters(method = "testMatchGivenNameLegal_FullShouldScore20," +
      "testMatchGivenNameUsual_FullShouldScore20," +
      "testMatchGivenNameUsual_AlternateLegalShouldScore20," +
      "testMatchGivenNameUsual_AlternateUsualShouldScore20," +
      "testMatchGivenNameLegal_4CharShouldScore15," +
      "testMatchGivenNameUsual_4CharShouldScore15," +
      "testMatchGivenNameAlternateUsual_4CharShouldScore15," +
      "testMatchGivenNameAlternateLegal_4CharShouldScore15," +
      "testMatchGivenNameLegal_1CharShouldScore5," +
      "testMatchGivenNameUsual_1CharShouldScore5," +
      "testMatchGivenNameAlternateUsual_1CharShouldScore5," +
      "testMatchGivenNameAlternateLegal_1CharShouldScore5," +
      "testMatchGivenNameLegal_10CharShouldScore20," +
      "testMatchGivenNameUsual_10CharShouldScore20," +
      "testMatchGivenNameAlternateUsual_10CharShouldScore20," +
      "testMatchGivenNameAlternateLegal_10CharShouldScore20," +
      "testMatchGivenNameLegal_SubsetShouldScore15," +
      "testMatchGivenNameUsual_SubsetShouldScore15," +
      "testMatchGivenNameAlternateUsual_SubsetShouldScore15," +
      "testMatchGivenNameAlternateLegal_SubsetShouldScore15," +
      "testMatchGivenNameLegalToGiven_SubsetShouldScore10," +
      "testMatchGivenNameUsualToGiven_SubsetShouldScore10," +
      "testMatchGivenNameAlternateUsualToGiven_SubsetShouldScore10," +
      "testMatchGivenNameAlternateLegalToGiven_SubsetShouldScore10," +
      "testMatchGivenNameAlternateLegalToUsualGiven_SubsetShouldScore10," +
      "testMatchGivenNameAlternateLegalToAltLegalGiven_SubsetShouldScore10," +
      "testMatchGivenNameAlternateLegalToAltUsualGiven_SubsetShouldScore10," +
      "testMatchGivenNameNickname1_SubsetShouldScore10," +
      "testMatchGivenNameNickname2_SubsetShouldScore10," +
      "testMatchGivenNameNickname3_SubsetShouldScore10," +
      "testMatchGivenNameNickname4_SubsetShouldScore10," )
  public void testMatchGivenNames_givenDifferentInputs_shouldReturnExpectedResults(PenMatchNames penMatchTransactionNames, PenMatchNames penMatchMasterNames, int expectedPoints, boolean isGivenNameFlip) {
    GivenNameMatchResult result = ScoringUtils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
    assertThat(result.getGivenNamePoints()).isEqualTo(expectedPoints);
    assertThat(result.isGivenNameFlip()).isEqualTo(isGivenNameFlip);
  }

  @Test
  @Parameters(method = "testMatchMiddleNameLegal_FullShouldScore20," +
      "testMatchMiddleNameUsual_FullShouldScore20," +
      "testMatchMiddleNameUsual_AlternateLegalShouldScore20," +
      "testMatchMiddleNameUsual_AlternateUsualShouldScore20," +
      "testMatchMiddleNameLegal_4CharShouldScore15," +
      "testMatchMiddleNameUsual_4CharShouldScore15," +
      "testMatchMiddleNameAlternateUsual_4CharShouldScore15," +
      "testMatchMiddleNameAlternateLegal_4CharShouldScore15," +
      "testMatchMiddleNameLegal_1CharShouldScore5," +
      "testMatchMiddleNameUsual_1CharShouldScore5," +
      "testMatchMiddleNameAlternateUsual_1CharShouldScore5," +
      "testMatchMiddleNameAlternateLegal_1CharShouldScore5," +
      "testMatchMiddleNameLegal_10CharShouldScore20," +
      "testMatchMiddleNameUsual_10CharShouldScore20," +
      "testMatchMiddleNameAlternateUsual_10CharShouldScore20," +
      "testMatchMiddleNameAlternateLegal_10CharShouldScore20," +
      "testMatchMiddleNameLegal_SubsetShouldScore15," +
      "testMatchMiddleNameUsual_SubsetShouldScore15," +
      "testMatchMiddleNameAlternateUsual_SubsetShouldScore15," +
      "testMatchMiddleNameAlternateLegal_SubsetShouldScore15," +
      "testMatchMiddleNameLegalToGiven_SubsetShouldScore10," +
      "testMatchMiddleNameUsualToGiven_SubsetShouldScore10," +
      "testMatchMiddleNameAlternateUsualToGiven_SubsetShouldScore10," +
      "testMatchMiddleNameAlternateLegalToGiven_SubsetShouldScore10," +
      "testMatchMiddleNameAlternateLegalToUsualGiven_SubsetShouldScore10," +
      "testMatchMiddleNameAlternateLegalToAltLegalGiven_SubsetShouldScore10," +
      "testMatchMiddleNameAlternateLegalToAltUsualGiven_SubsetShouldScore10" )
  public void testMatchMiddleNames_givenDifferentInputs_shouldReturnExpectedResults(PenMatchNames penMatchTransactionNames, PenMatchNames penMatchMasterNames, int expectedPoints, boolean isMiddleNameFlip) {
    MiddleNameMatchResult result = ScoringUtils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
    assertThat(result.getMiddleNamePoints()).isEqualTo(expectedPoints);
    assertThat(result.isMiddleNameFlip()).isEqualTo(isMiddleNameFlip);
  }


  @Test
  @Parameters(method = "testMatchSurnameLegal_ShouldScore20," +
      "testMatchSurnameUsual_ShouldScore20," +
      "testMatchSurnameLegalToUsual_ShouldScore20," +
      "testMatchSurnameUsualToLegal_ShouldScore20," +
      "testMatchSurnameLegal4Char_ShouldScore10," +
      "testMatchSurnameUsualToLegal4Char_ShouldScore10," +
      "testMatchSurnameLegalToUsual4Char_ShouldScore10," +
      "testMatchSurnameUsualToUsual4Char_ShouldScore10")
  public void testMatchSurname_givenDifferentInputs_shouldReturnExpectedResults(PenMatchStudentDetail student,PenMasterRecord master, int expectedScore, boolean isLegalSurnameUsed ) {
    SurnameMatchResult result = ScoringUtils.matchSurname(student, master);
    assertThat(result.getSurnamePoints()).isEqualTo(expectedScore);
    assertThat(result.isLegalSurnameUsed()).isEqualTo(isLegalSurnameUsed);
  }


  @Test
  public void testMatchLegalSurnameSoundex_ShouldScore10() {

    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Micheals");
    master.setSurname("Micells");
    SurnameMatchResult result = ScoringUtils.matchSurname(student, master);
    assertEquals(10, (int) result.getSurnamePoints());
  }

  @Test
  public void testMatchUsualSurnameSoundex_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    master.setSurname(null);
    student.setUsualSurname("Micells");
    master.setUsualSurname("Micheals");
    SurnameMatchResult result = ScoringUtils.matchSurname(student, master);
    assertEquals(10, (int) result.getSurnamePoints());
  }

  @Test
  public void testMatchUsualToLegalSurnameSoundex_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Micells");
    master.setSurname(null);
    student.setUsualSurname(null);
    master.setUsualSurname("Micheals");
    SurnameMatchResult result = ScoringUtils.matchSurname(student, master);
    assertEquals(10, (int) result.getSurnamePoints());
  }

  @Test
  public void testMatchLegalToUsualSurnameSoundex_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    master.setSurname("Micells");
    student.setUsualSurname("Micheals");
    master.setUsualSurname(null);
    SurnameMatchResult result = ScoringUtils.matchSurname(student, master);
    assertEquals(10, (int) result.getSurnamePoints());
  }

  @Test
  public void testMatchSex_ShouldScore0() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSex("M");
    master.setSex("F");

    assertEquals(0, ScoringUtils.matchSex(student, master));
  }

  public PenMasterRecord createPenMasterRecord() {
    PenMasterRecord masterRecord = new PenMasterRecord();

    masterRecord.setPen("12345647");
    masterRecord.setDob("19800518");
    masterRecord.setSurname("JACKSON");
    masterRecord.setGiven("PETER");
    masterRecord.setMiddle(null);
    masterRecord.setUsualSurname(null);
    masterRecord.setUsualGivenName(null);
    masterRecord.setUsualMiddleName(null);
    masterRecord.setPostal(null);
    masterRecord.setSex("M");
    masterRecord.setGrade("10");
    masterRecord.setStatus(PenStatus.AA.getValue());
    masterRecord.setMincode("123456978");
    masterRecord.setLocalId(null);

    return masterRecord;
  }

  private PenMatchStudentDetail createPenMatchStudentDetail() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen(null);
    student.setSurname("JACKSON");
    student.setGivenName("MIKE");
    student.setMiddleName(null);
    student.setUsualSurname(null);
    student.setUsualGivenName(null);
    student.setUsualMiddleName(null);
    student.setPostal(null);
    student.setDob("19800518");
    student.setLocalID(null);
    student.setSex("M");
    student.setMincode("12345567");
    student.setUpdateCode(null);

    student.setEnrolledGradeCode(null);

    return student;
  }


  public Object testMatchLocalID_AlternateLocalIdScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("123456789");
    student.setMincode("987654321");
    master.setMincode("987654321");
    master.setAlternateLocalId("123456789");
    PenMatchSession session = new PenMatchSession();
    student.setAlternateLocalID("123456789");
    return new Object[]{
        student, master, session, 20
    };
  }

  public Object testMatchLocalID_ShouldScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("123456789");
    student.setMincode("987654321");
    master.setLocalId("123456789");
    master.setMincode("987654321");
    master.setAlternateLocalId("123456789");

    PenMatchSession session = new PenMatchSession();
    student.setAlternateLocalID("123456789");
    return new Object[]{
        student, master, session, 20
    };
  }

  public Object testMatchLocalIDSameSchool_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("123456789");
    student.setMincode("987654321");
    master.setLocalId("123456788");
    master.setMincode("987654321");

    PenMatchSession session = new PenMatchSession();
    return new Object[]{
        student, master, session, 10
    };
  }

  public Object testMatchLocalIDSameDistrict_ShouldScore5() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("12388888");
    student.setMincode("987654321");
    master.setLocalId("123456788");
    master.setMincode("987884321");

    PenMatchSession session = new PenMatchSession();

    return new Object[]{
        student, master, session, 5
    };
  }

  public Object testMatchLocalIDSameDistrict102_ShouldScore0() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("12388888");
    student.setMincode("102654321");
    master.setLocalId("123456788");
    master.setMincode("102884321");

    PenMatchSession session = new PenMatchSession();
    return new Object[]{
        student, master, session, 0
    };
  }

  public Object testMatchLocalIDWithDemerits_ShouldScore10Demerits() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("123456789");
    student.setMincode("123456788");
    master.setLocalId("123456788");
    master.setMincode("123456788");

    PenMatchSession session = new PenMatchSession();
    return new Object[]{
        student, master, session, 10, 10
    };
  }

  public Object testMatchLocalIDWithDemerits_ShouldScore10DemeritsWithAlternate() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setLocalID("123456789");
    student.setMincode("987654321");
    master.setMincode("987654321");
    master.setAlternateLocalId("123456788");

    PenMatchSession session = new PenMatchSession();
    student.setAlternateLocalID("123456789");
    return new Object[]{
        student, master, session, 10, 10
    };
  }

  public Object testMatchGivenNameLegal_FullShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("MichealsJ");
    penMatchMasterNames.setLegalGiven("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameUsual_FullShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("MichealsJ");
    penMatchMasterNames.setUsualGiven("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameUsual_AlternateLegalShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("MichealsJ");
    penMatchMasterNames.setAlternateLegalGiven("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }


  public Object testMatchGivenNameUsual_AlternateUsualShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("MichealsJ");
    penMatchMasterNames.setAlternateUsualGiven("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameLegal_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("Michs");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }


  public Object testMatchGivenNameUsual_4CharShouldScore15() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("Michs");
    penMatchMasterNames.setUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }
  public Object testMatchGivenNameAlternateUsual_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("Michs");
    penMatchMasterNames.setAlternateUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchGivenNameAlternateLegal_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michs");
    penMatchMasterNames.setAlternateLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }
  public Object testMatchGivenNameLegal_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("Marcs");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchGivenNameUsual_1CharShouldScore5() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("Marcs");
    penMatchMasterNames.setUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchGivenNameAlternateUsual_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("Marcs");
    penMatchMasterNames.setAlternateUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }
  public Object testMatchGivenNameAlternateLegal_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Marcs");
    penMatchMasterNames.setAlternateLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchGivenNameLegal_10CharShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("Michealalas");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameUsual_10CharShouldScore20() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("Michealalas");
    penMatchMasterNames.setUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }
  public Object testMatchGivenNameAlternateUsual_10CharShouldScore20() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("Michealalas");
    penMatchMasterNames.setAlternateUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameAlternateLegal_10CharShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michealalas");
    penMatchMasterNames.setAlternateLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchGivenNameLegal_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("alalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchGivenNameUsual_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("alalad");
    penMatchMasterNames.setUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchGivenNameAlternateUsual_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("alalad");
    penMatchMasterNames.setAlternateUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchGivenNameAlternateLegal_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("alalad");
    penMatchMasterNames.setAlternateLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchGivenNameLegalToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalGiven("Michealalad");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameUsualToGiven_SubsetShouldScore10() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualGiven("Michealalad");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameAlternateUsualToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualGiven("Michealalad");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameAlternateLegalToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameAlternateLegalToUsualGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
    penMatchMasterNames.setUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameAlternateLegalToAltLegalGiven_SubsetShouldScore10() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
    penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameAlternateLegalToAltUsualGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
    penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchGivenNameNickname1_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setNickname1("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, false
    };
  }
  public Object testMatchGivenNameNickname2_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setNickname2("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, false
    };
  }

  public Object testMatchGivenNameNickname3_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setNickname3("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, false
    };
  }

  public Object testMatchGivenNameNickname4_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setNickname4("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, false
    };
  }

  public Object testMatchMiddleNameLegal_FullShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("MichealsJ");
    penMatchMasterNames.setLegalMiddle("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }
  public Object testMatchMiddleNameUsual_FullShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("MichealsJ");
    penMatchMasterNames.setUsualMiddle("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }
  public Object testMatchMiddleNameUsual_AlternateLegalShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("MichealsJ");
    penMatchMasterNames.setAlternateLegalMiddle("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchMiddleNameUsual_AlternateUsualShouldScore20() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("MichealsJ");
    penMatchMasterNames.setAlternateUsualMiddle("MichealsJ");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }
  public Object testMatchMiddleNameLegal_4CharShouldScore15() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("Michs");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameUsual_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("Michs");
    penMatchMasterNames.setUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameAlternateUsual_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("Michs");
    penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameAlternateLegal_4CharShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michs");
    penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameLegal_1CharShouldScore5() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("Marcs");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchMiddleNameUsual_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("Marcs");
    penMatchMasterNames.setUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchMiddleNameAlternateUsual_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("Marcs");
    penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchMiddleNameAlternateLegal_1CharShouldScore5() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Marcs");
    penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 5, false
    };
  }

  public Object testMatchMiddleNameLegal_10CharShouldScore20() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("Michealalas");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchMiddleNameUsual_10CharShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("Michealalas");
    penMatchMasterNames.setUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchMiddleNameAlternateUsual_10CharShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("Michealalas");
    penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchMiddleNameAlternateLegal_10CharShouldScore20() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michealalas");
    penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 20, false
    };
  }

  public Object testMatchMiddleNameLegal_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("alalad");
    penMatchMasterNames.setLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameUsual_SubsetShouldScore15() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("alalad");
    penMatchMasterNames.setUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameAlternateUsual_SubsetShouldScore15() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("alalad");
    penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameAlternateLegal_SubsetShouldScore15() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("alalad");
    penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 15, false
    };
  }

  public Object testMatchMiddleNameLegalToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setLegalMiddle("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }
  public Object testMatchMiddleNameUsualToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setUsualMiddle("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchMiddleNameAlternateUsualToGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateUsualMiddle("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchMiddleNameAlternateLegalToGiven_SubsetShouldScore10() {

    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
    penMatchMasterNames.setLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchMiddleNameAlternateLegalToUsualGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
    penMatchMasterNames.setUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchMiddleNameAlternateLegalToAltLegalGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
    penMatchMasterNames.setAlternateLegalGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchMiddleNameAlternateLegalToAltUsualGiven_SubsetShouldScore10() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    PenMatchNames penMatchMasterNames = new PenMatchNames();
    penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
    penMatchMasterNames.setAlternateUsualGiven("Michealalad");
    return new Object[]{
        penMatchTransactionNames, penMatchMasterNames, 10, true
    };
  }

  public Object testMatchSurnameLegal_ShouldScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Micheals");
    master.setSurname("Micheals");
    return new Object[]{
        student, master, 20, true
    };
  }

  public Object testMatchSurnameUsual_ShouldScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    master.setSurname(null);
    student.setUsualSurname("Micheals");
    master.setUsualSurname("Micheals");
    return new Object[]{
        student, master, 20, false
    };
  }

  public Object testMatchSurnameLegalToUsual_ShouldScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Micheals");
    student.setUsualSurname(null);
    master.setSurname(null);
    master.setUsualSurname("Micheals");
    return new Object[]{
        student, master, 20, true
    };
  }

  public Object testMatchSurnameUsualToLegal_ShouldScore20() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    student.setUsualSurname("Micheals");
    master.setSurname("Micheals");
    master.setUsualSurname(null);
    return new Object[]{
        student, master, 20, false
    };
  }

  public Object testMatchSurnameLegal4Char_ShouldScore10() {

    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Michells");
    master.setSurname("Micheals");
    return new Object[]{
        student, master, 10, false
    };
  }

  public Object testMatchSurnameUsualToLegal4Char_ShouldScore10() {

    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname("Michells");
    master.setSurname(null);
    student.setUsualSurname(null);
    master.setUsualSurname("Micheals");
    return new Object[]{
        student, master, 10, false
    };
  }

  public Object testMatchSurnameLegalToUsual4Char_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    master.setSurname("Michells");
    student.setUsualSurname("Micheals");
    master.setUsualSurname(null);
    return new Object[]{
        student, master, 10, false
    };
  }

  public Object testMatchSurnameUsualToUsual4Char_ShouldScore10() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    PenMasterRecord master = createPenMasterRecord();
    student.setSurname(null);
    master.setSurname(null);
    student.setUsualSurname("Michichy");
    master.setUsualSurname("Michells");
    return new Object[]{
        student, master, 10, false
    };
  }
}
