package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.v1.*;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
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
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

      final File fileMatch = new File("src/test/resources/mock_match_codes.json");
      List<MatchCodeEntity> matchCodes = objectMapper.readValue(fileMatch, new TypeReference<>() {
      });
      matchCodesRepository.saveAll(matchCodes);

      final File fileNick = new File("src/test/resources/mock_nicknames.json");
      List<NicknameEntity> nicknameEntities = objectMapper.readValue(fileNick, new TypeReference<>() {
      });
      nicknamesRepository.saveAll(nicknameEntities);

      final File fileSurnameFrequency = new File("src/test/resources/mock_surname_frequency.json");
      List<FrequencySurnameEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFrequency, new TypeReference<>() {
      });
      surnameFreqRepository.saveAll(surnameFreqEntities);
      dataLoaded = true;
    }
    lookupManager.init();
  }

  @After
  public void tearDown() {
    matchCodesRepository.deleteAll();
    nicknamesRepository.deleteAll();
    surnameFreqRepository.deleteAll();
    dataLoaded = false;
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
}
