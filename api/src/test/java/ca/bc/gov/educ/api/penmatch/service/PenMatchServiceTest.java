package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.v1.MatchCodesEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchCodesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.v1.match.PenMatchService;
import ca.bc.gov.educ.api.penmatch.service.v1.match.SurnameFrequencyService;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.penmatch.constants.PenStatus.AA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * The type Pen match service test.
 */
@RunWith(JUnitParamsRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class PenMatchServiceTest {
  /**
   * The constant scr.
   */
  @ClassRule
  public static final SpringClassRule scr = new SpringClassRule();
  /**
   * The constant dataLoaded.
   */
  private static boolean dataLoaded = false;
  /**
   * The Smr.
   */
  @Rule
  public final SpringMethodRule smr = new SpringMethodRule();
  /**
   * The Rest utils.
   */
  @MockBean
  RestUtils restUtils;
  /**
   * The Nicknames repository.
   */
  @Autowired
  NicknamesRepository nicknamesRepository;
  /**
   * The Surname freq repository.
   */
  @Autowired
  SurnameFrequencyRepository surnameFreqRepository;

  @Autowired
  SurnameFrequencyService surnameFrequencyService;
  /**
   * The Service.
   */
  @Autowired
  private PenMatchService service;

  /**
   * The Lookup manager.
   */
  @Autowired
  private PenMatchLookupManager lookupManager;
  /**
   * The Match codes repository.
   */
  @Autowired
  MatchCodesRepository matchCodesRepository;

  /**
   * The Correlation id.
   */
  UUID correlationID = UUID.randomUUID();

  /**
   * Sets .
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    MockitoAnnotations.openMocks(this);
    if (!dataLoaded) {
      matchCodesRepository.saveAll(getMatchCodes());
      final File fileNick = new File("src/test/resources/mock_nicknames.json");
      List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<>() {
      });
      nicknamesRepository.saveAll(nicknameEntities);

      final File fileSurnameFrequency = new File("src/test/resources/mock_surname_frequency.json");
      List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFrequency, new TypeReference<>() {
      });
      surnameFreqRepository.saveAll(surnameFreqEntities);
      dataLoaded = true;
    }
    lookupManager.init();
  }

  /**
   * Test match student alg 30 should return status d 1 match.
   * below are parameters are in order
   * pen, given name, middle name, postal code, sex, expected pen status
   *
   * @param pen                 the pen
   * @param givenName           the given name
   * @param middleName          the middle name
   * @param postalCode          the postal code
   * @param sex                 the sex
   * @param dob                 the dob
   * @param localID             the local id
   * @param expectedMatchStatus the expected match status
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  @Parameters({
      "null,null,LUKE,V1B1J0,M,19791018,285261,F1", // ALG 30
      "null,null,null,null,F,19791018,285261,F1", // ALG 40
      "null,null,null,null,M,19791018,285261,F1", // ALG 50
      "null,VICTORIA,WILLIAM,null,M,19981102,239661,D1",
      "null,WILLIAM,VICTORIA,null,M,19981102,239661,D1",
      "null,PETE,VICTORIA,null,M,19981102,239661,D1",
      "null,PETE,YARN,null,M,19981102,239661,D1",
      "null,JAKE,YARN,null,M,19981102,239661,D1",
      "null,JAKE,YARN,null,F,19981102,239661,D1",
      "null,JAKE,YARN,V8R4N4,F,19981102,239661,D1",
      "null,JAKE,YARN,null,F,19920223,239661,D1",
  })
  public void testMatchStudent_givenDifferentParameters_ShouldReturnStatusF1Match(String pen, String givenName, String middleName,
                                                                                  String postalCode, String sex, String dob, String localID, String expectedMatchStatus) throws JsonProcessingException {

    PenMatchStudentDetail student = createPenMatchStudentDetail();
    student.setPen("null".equals(pen) ? null : pen);
    student.setGivenName("null".equals(givenName) ? null : givenName);
    student.setMiddleName("null".equals(middleName) ? null : middleName);
    student.setPostal("null".equals(postalCode) ? null : postalCode);
    student.setSex("null".equals(sex) ? null : sex);
    student.setDob("null".equals(dob) ? null : dob);
    student.setLocalID("null".equals(localID) ? null : localID);
    if ("PETE".equalsIgnoreCase(givenName) || "YARN".equalsIgnoreCase(middleName)) {
      student.setUpdateCode("S");
    }
    if ("JAKE".equalsIgnoreCase(givenName)) {
      student.setUpdateCode("S");
      student.setSurname("JAKE");
    }
    List<StudentEntity> students = new ArrayList<>();
    students.add(createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID()));
    when(restUtils.lookupWithAllParts(student.getDob(), student.getSurname(), student.getGivenName() == null ? null : student.getGivenName().substring(0, 1), student.getMincode(), student.getLocalID(), correlationID)).thenReturn(students);
    when(restUtils.lookupNoInit(student.getDob(), student.getSurname(), student.getMincode(), student.getLocalID(), correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    if (!"JAKE".equalsIgnoreCase(givenName)) {
      verify(restUtils, atLeastOnce()).lookupWithAllParts(student.getDob(), student.getSurname(), student.getGivenName() == null ? null : student.getGivenName().substring(0, 1), student.getMincode(), student.getLocalID(), correlationID);
    } else {
      verify(restUtils, atLeastOnce()).lookupNoInit(student.getDob(), student.getSurname(), student.getMincode(), student.getLocalID(), correlationID);
    }

    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(expectedMatchStatus);
  }


  /**
   * Test match student when payload is valid alg 51 should return d 1 match.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testMatchStudent_WhenPayloadIsValidAlg51_ShouldReturnD1Match() throws JsonProcessingException {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    student.setPen(null);
    student.setGivenName("CLA");
    student.setMiddleName(null);
    student.setSex("F");
    student.setDob("19990501");
    student.setLocalID(null);
    List<StudentEntity> students = new ArrayList<>();
    students.add(createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID()));
    when(restUtils.lookupNoLocalID(student.getDob(), student.getSurname(), "C", correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).lookupNoLocalID(student.getDob(), student.getSurname(), "C", correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.D1.toString());
  }

  /**
   * Test match student without pen should return d 1 match.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testMatchStudentWithoutPEN_ShouldReturnD1Match() throws JsonProcessingException {
    PenMatchStudentDetail student = createPenMatchStudentDetailWithoutPEN();
    var studentEntity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    List<StudentEntity> students = new ArrayList<>();
    students.add(studentEntity);
    when(restUtils.lookupWithAllParts(student.getDob(), student.getSurname(), "C", student.getMincode(), student.getLocalID(), correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).lookupWithAllParts(student.getDob(), student.getSurname(), "C", student.getMincode(), student.getLocalID(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());

    assertThat(result.getPenStatus()).isEqualTo(PenStatus.D1.toString());
  }

  /**
   * Test match student without pen no local id should return d 1 match.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testMatchStudentWithoutPENNoLocalID_ShouldReturnF1Match() throws JsonProcessingException {
    PenMatchStudentDetail student = createPenMatchStudentDetailWithoutPEN();
    student.setLocalID(null);
    var studentEntity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    List<StudentEntity> students = new ArrayList<>();
    students.add(studentEntity);
    when(restUtils.lookupNoLocalID(student.getDob(), student.getSurname(), "C", correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).lookupNoLocalID(student.getDob(), student.getSurname(), "C", correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.F1.toString());
  }


  /**
   * Test match student for core check should return c 0.
   */
  @Test
  public void testMatchStudentForCoreCheck_ShouldReturnC0() {
    PenMatchStudentDetail student = createPenMatchStudentDetailForCoreCheck();
    PenMatchResult result = service.matchStudent(student, correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.C0.toString());
  }

  /**
   * Test match student valid twin should return aa.
   */
  @Test
  public void testMatchStudentValidTwin_ShouldReturnAA() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    student.setLocalID("285262");
    student.setGivenName("CLAYTON");

    StudentEntity entity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    entity.setPen(student.getPen());
    when(restUtils.getPenMasterRecordByPen(student.getPen(), correlationID)).thenReturn(createPenMasterRecord(entity));

    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).getPenMasterRecordByPen(student.getPen(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(AA.toString());
  }

  /**
   * Test match student should return aa match.
   */
  @Test
  public void testMatchStudent_ShouldReturnAAMatch() {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    StudentEntity entity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    entity.setPen(student.getPen());
    when(restUtils.getPenMasterRecordByPen(student.getPen(), correlationID)).thenReturn(createPenMasterRecord(entity));
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).getPenMasterRecordByPen(student.getPen(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(AA.toString());
  }

  /**
   * Test match student in valid pen should return c 1 match.
   *
   * @param pen the pen
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  @Parameters({
      "123456888",
      "109508853",
      "123456789",
      "987654321",
      "154632789",
      "111111111",
      "222222222",
      "333333333",

  })
  public void testMatchStudentInValidPEN_ShouldReturnC1Match(String pen) throws JsonProcessingException {
    PenMatchStudentDetail student = createPenMatchStudentDetail();
    student.setPen(pen);
    var studentEntity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    List<StudentEntity> students = new ArrayList<>();
    students.add(studentEntity);
    when(restUtils.lookupWithAllParts(student.getDob(), student.getSurname(), "C", student.getMincode(), student.getLocalID(), correlationID)).thenReturn(students);
    when(restUtils.lookupNoLocalID(student.getDob(), student.getSurname(), "C", correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).lookupWithAllParts(student.getDob(), student.getSurname(), "C", student.getMincode(), student.getLocalID(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.C1.toString());
  }


  /**
   * Test match student no matches should return d 0.
   */
  @Test
  public void testMatchStudentNoMatches_ShouldReturnD0() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen(null);
    student.setSurname("PASCAL");
    student.setGivenName("JOSEPH");
    student.setMiddleName(null);
    student.setUsualSurname("PASCAL");
    student.setUsualGivenName("JOSEPH");
    student.setUsualMiddleName(null);
    student.setPostal(null);
    student.setDob("19601120");
    student.setLocalID("32342");
    student.setSex("X");
    PenMatchResult result = service.matchStudent(student, correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.D0.toString());
  }


  /**
   * Test match student with full no sex should return aa match.
   */
  @Test
  public void testMatchStudentWithFullNoSex_ShouldReturnAAMatch() {
    PenMatchStudentDetail student = createPenMatchFullStudent();
    student.setSex(null);
    student.setDob("19991201");
    StudentEntity entity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    entity.setPen(student.getPen());
    when(restUtils.getPenMasterRecordByPen(student.getPen(), correlationID)).thenReturn(createPenMasterRecord(entity));

    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).getPenMasterRecordByPen(student.getPen(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(AA.toString());
  }

  /**
   * Test match student with merged deceased should return c 0.
   */
  @Test
  public void testMatchStudentWithMergedDeceased_ShouldReturnC0() {
    PenMatchStudentDetail student = createPenMatchStudentDetailMergedDeceased();
    PenMatchResult result = service.matchStudent(student, correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertEquals(result.getPenStatus(), PenStatus.C0.toString());
  }


  /**
   * Test match student with rare name with update code y should return c 0.
   */
  @Test
  public void testMatchStudentWithRareNameWithUpdateCodeY_ShouldReturnC0() {
    PenMatchStudentDetail student = createPenMatchStudentDetailMergedDeceased();
    student.setUpdateCode("Y");
    student.setMincode("00501007");
    StudentEntity entity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    entity.setPen(student.getPen());

    when(restUtils.getPenMasterRecordByPen(student.getPen(), correlationID)).thenReturn(Optional.empty());

    PenMatchResult result = service.matchStudent(student, correlationID);

    verify(restUtils, atLeastOnce()).getPenMasterRecordByPen(student.getPen(), correlationID);

    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getPenStatus()).isEqualTo(PenStatus.C0.toString());
  }

  /**
   * Test match student with merged valid given old match returns f 1 should execute new pen match.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testMatchStudentWithMergedValid_givenOldMatchReturnsF1_shouldExecuteNewPenMatch() throws JsonProcessingException {
    PenMatchStudentDetail student = createPenMatchStudentDetailMergedValid();
    StudentEntity entity = createStudent(student.getDob(), student.getSurname(), student.getGivenName(), student.getMincode(), student.getLocalID());
    entity.setPen(student.getPen());
    when(restUtils.getPenMasterRecordByPen(student.getPen(), correlationID)).thenReturn(createPenMasterRecord(entity));
    var students = new ArrayList<StudentEntity>();
    students.add(entity);
    when(restUtils.lookupNoInitNoLocalID(student.getDob(), "SMIT", correlationID)).thenReturn(students);
    PenMatchResult result = service.matchStudent(student, correlationID);
    verify(restUtils, atLeastOnce()).lookupNoInitNoLocalID(student.getDob(), "SMIT", correlationID);
    verify(restUtils, atLeastOnce()).getPenMasterRecordByPen(student.getPen(), correlationID);
    assertNotNull(result);
    assertNotNull(result.getPenStatus());
    assertThat(result.getMatchingRecords()).size().isEqualTo(1);
    assertThat(result.getPenStatus()).isEqualTo(AA.toString());
  }

  /**
   * Create pen match student detail pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchStudentDetail() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen("122740046");
    student.setSurname("LORD");
    student.setGivenName("CLAYTON");
    student.setMiddleName("L");
    student.setDob("19991201");
    student.setLocalID("285261");
    student.setSex("F");
    student.setMincode("00501007");

    return student;
  }

  /**
   * Create pen match full student pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchFullStudent() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen("122740046");
    student.setSurname("LORD");
    student.setGivenName("CLAYTON");
    student.setMiddleName("L");
    student.setDob("19990112");
    student.setLocalID("285261");
    student.setSex("F");

    student.setMincode("00501007");

    return student;
  }

  /**
   * Create pen match student detail without pen pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchStudentDetailWithoutPEN() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setSurname("LORD");
    student.setGivenName("CLAYTON");
    student.setMiddleName("L");
    student.setDob("19991201");
    student.setLocalID("285261");
    student.setSex("F");

    return student;
  }


  /**
   * Create pen match student detail merged deceased pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchStudentDetailMergedDeceased() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen("108999400");
    student.setSurname("VANDERLEEK");
    student.setGivenName("JAKE");
    student.setMiddleName("WILLIAM");
    student.setUsualSurname("VANDERLEEK");
    student.setUsualGivenName("JAKE");
    student.setUsualMiddleName("WILLIAM");
    student.setDob("19791018");
    student.setLocalID("285261");
    student.setSex("M");

    return student;
  }


  /**
   * Create pen match student detail for core check pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchStudentDetailForCoreCheck() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen("113874041");
    student.setSurname("VANDERLEEK");
    student.setGivenName("JAKE");
    student.setMiddleName("WILLIAM");
    student.setUsualSurname("VANDERLEEK");
    student.setUsualGivenName("JAKE");
    student.setUsualMiddleName("WILLIAM");
    student.setPostal(null);
    student.setDob("19791018");
    student.setLocalID("285261");
    student.setSex("M");
    student.setMincode("08288006");
    student.setUpdateCode("Y");

    return student;
  }

  /**
   * Create student student entity.
   *
   * @param dob       the dob
   * @param surname   the surname
   * @param givenName the given name
   * @param mincode   the mincode
   * @param localID   the local id
   * @return the student entity
   */
  private StudentEntity createStudent(String dob, String surname, String givenName, String mincode, String localID) {
    return StudentEntity.builder()
        .dob(dob.substring(0, 4).concat("-").concat(dob.substring(4, 6).concat("-").concat(dob.substring(6, 8))))
        .legalLastName(surname)
        .legalFirstName(givenName)
        .mincode(mincode)
        .localID(localID)
        .statusCode("A")
        .studentID(UUID.randomUUID())
        .email("testemail@gmail.com")
        .gradeCode("01")
        .genderCode("M")
        .pen("200000008")
        .build();
  }

  /**
   * Create pen master record optional.
   *
   * @param student the student
   * @return the optional
   */
  private Optional<PenMasterRecord> createPenMasterRecord(StudentEntity student) {
    return Optional.of(PenMatchUtils.convertStudentEntityToPenMasterRecord(student));
  }

  /**
   * Create pen match student detail merged valid pen match student detail.
   *
   * @return the pen match student detail
   */
  private PenMatchStudentDetail createPenMatchStudentDetailMergedValid() {
    PenMatchStudentDetail student = new PenMatchStudentDetail();
    student.setPen("113874044");
    student.setSurname("SMITH");
    student.setGivenName("JOE");
    student.setMiddleName("JAMES");
    student.setUsualSurname("SMITH");
    student.setUsualGivenName("JOE");
    student.setUsualMiddleName("JAMES");
    student.setDob("19800410");
    student.setSex("M");

    return student;
  }

  /**
   * Gets match codes.
   *
   * @return the match codes
   * @throws JsonProcessingException the json processing exception
   */
  private List<MatchCodesEntity> getMatchCodes() throws JsonProcessingException {
    String matchCodesJson = "[\n" +
        "  {\n" +
        "    \"matchCode\": \"1111111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111112\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111121\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111122\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111211\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111212\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111221\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1111332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112231\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1112332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1121332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122331\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1122332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131112\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131122\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131131\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131212\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131221\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131331\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1131332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132331\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1132332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141112\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141121\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141122\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141131\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141211\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141212\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141221\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1141332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1142332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1211332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1212332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1221332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1222332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1231332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1232332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1241332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1242332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311122\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311212\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311221\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1311332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312111\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1312332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1321332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322111\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1322332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331122\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331212\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1331332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332211\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1332332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341111\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341122\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341212\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341221\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341232\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341322\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1341332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342111\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"1342332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111131\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2111332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2112332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2121332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2122332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131121\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2131332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2132332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141121\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141131\",\n" +
        "    \"matchResult\": \"P\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141211\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2141332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2142332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2211332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2212332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2221332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2222332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2231332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2232332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2241332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2242332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311112\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2311332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312111\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2312332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2321332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322111\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322131\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322311\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322331\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2322332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2331332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2332332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341111\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341112\",\n" +
        "    \"matchResult\": \"Q\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341121\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341132\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341211\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341231\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341312\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341321\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2341332\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342111\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342112\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342121\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342122\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342131\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342132\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342211\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342212\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342221\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342222\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342231\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342232\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342311\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342312\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342321\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342322\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342331\",\n" +
        "    \"matchResult\": \" \"\n" +
        "  },\n" +
        "  {\n" +
        "    \"matchCode\": \"2342332\",\n" +
        "    \"matchResult\": \"F\"\n" +
        "  }\n" +
        "]";
    TypeReference<List<MatchCodesEntity>> type = new TypeReference<>() {
    };
    return new ObjectMapper().readValue(matchCodesJson, type);
  }
}
