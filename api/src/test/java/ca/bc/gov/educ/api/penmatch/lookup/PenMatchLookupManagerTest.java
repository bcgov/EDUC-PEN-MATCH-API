package ca.bc.gov.educ.api.penmatch.lookup;

import ca.bc.gov.educ.api.penmatch.model.v1.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.repository.v1.ForeignSurnameRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchCodesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.v1.match.SurnameFrequencyService;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * The type Pen match lookup manager test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchLookupManagerTest {
  /**
   * The constant lookupManager.
   */
  private static PenMatchLookupManager lookupManager;
  /**
   * The constant dataLoaded.
   */
  private static boolean dataLoaded = false;
  /**
   * The Rest utils.
   */
  @Autowired
  RestUtils restUtils;
  /**
   * The Rest template.
   */
  @MockBean
  RestTemplate restTemplate;

  /**
   * The Props.
   */
  @Autowired
  ApplicationProperties props;
  /**
   * The Foreign surname repository.
   */
  @Autowired
  ForeignSurnameRepository foreignSurnameRepository;
  /**
   * The Nicknames repository.
   */
  @Autowired
  NicknamesRepository nicknamesRepository;
  /**
   * The Match codes repository.
   */
  @Autowired
  MatchCodesRepository matchCodesRepository;
  /**
   * The Surname frequency repository.
   */
  @Autowired
  SurnameFrequencyRepository surnameFrequencyRepository;

  @Autowired
  SurnameFrequencyService surnameFrequencyService;
  /**
   * The Correlation id.
   */
  UUID correlationID = UUID.randomUUID();

  /**
   * Before.
   *
   * @throws Exception the exception
   */
  @Before
  public void before() throws Exception {
    if (!dataLoaded) {
      final File fileNick = new File("src/test/resources/mock_nicknames.json");
      List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<>() {
      });
      nicknamesRepository.saveAll(nicknameEntities);

      final File fileSurnameFrequency = new File("src/test/resources/mock_surname_frequency.json");
      List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFrequency, new TypeReference<>() {
      });
      surnameFrequencyRepository.saveAll(surnameFreqEntities);
      lookupManager = new PenMatchLookupManager(foreignSurnameRepository, nicknamesRepository, matchCodesRepository, restUtils, surnameFrequencyService);
      dataLoaded = true;
    }
  }

  /**
   * Test lookup surname frequency should return 0.
   */
  @Test
  public void testLookupSurnameFrequency_ShouldReturn0() {
    assertEquals(0, (int) lookupManager.lookupSurnameFrequency("ASDFJSD"));
  }

  /**
   * Test lookup surname frequency should return over 200.
   */
  @Test
  public void testLookupSurnameFrequency_ShouldReturnOver200() {
    assertTrue(lookupManager.lookupSurnameFrequency("JAM") > 200);
  }

  /**
   * Test lookup nicknames should return 4 names.
   */
  @Test
  public void testLookupNicknames_ShouldReturn4Names() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();
    lookupManager.init();
    lookupManager.lookupNicknames(penMatchTransactionNames, "JAMES");

    assertNotNull(penMatchTransactionNames.getNickname1());
    assertNotNull(penMatchTransactionNames.getNickname2());
    assertNotNull(penMatchTransactionNames.getNickname3());
    assertNotNull(penMatchTransactionNames.getNickname4());
  }

  /**
   * Test lookup student by pen.
   */
  @Test
  public void testLookupStudentByPEN() {
    when(restUtils.getPenMasterRecordByPen("108999400", correlationID)).thenReturn(Optional.of(new PenMasterRecord()));
    var masterRecord = lookupManager.lookupStudentByPEN("108999400", correlationID);

    assertThat(masterRecord).isPresent();
  }

  /**
   * Test lookup student with all parts.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentWithAllParts() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", "VICTORIA", "00501007", "239661"));
    when(restUtils.lookupWithAllParts("19981102", "ODLUS", "VICTORIA", "00501007", "239661", correlationID)).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupWithAllParts("19981102", "ODLUS", "VICTORIA", "00501007", "239661", correlationID);

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  /**
   * Test lookup student no init large data.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentNoInitLargeData() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", null, "VICTORIA", "00501007"));
    when(restUtils.lookupNoInit("19981102", "ODLUS", "VICTORIA", "00501007", correlationID)).thenReturn(students);

    List<StudentEntity> studentEntities = lookupManager.lookupNoInit("19981102", "ODLUS", "VICTORIA", "00501007", correlationID);
    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  /**
   * Test lookup student no init.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentNoInit() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", null, "JAKE", "08288006"));
    when(restUtils.lookupNoInit("19791018", "VANDERLEEK", "JAKE", "08288006", correlationID)).thenReturn(students);

    List<StudentEntity> studentEntities = lookupManager.lookupNoInit("19791018", "VANDERLEEK", "JAKE", "08288006", correlationID);

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  /**
   * Test lookup student no local id large data.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentNoLocalIDLargeData() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", "VICTORIA", null, null));
    when(restUtils.lookupNoLocalID("19981102", "ODLUS", "VICTORIA", correlationID)).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoLocalID("19981102", "ODLUS", "VICTORIA", correlationID);

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  /**
   * Test lookup student no local id.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentNoLocalID() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", "JAKE", null, null));
    when(restUtils.lookupNoLocalID("19791018", "VANDERLEEK", "JAKE", correlationID)).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoLocalID("19791018", "VANDERLEEK", "JAKE", correlationID);

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  /**
   * Test lookup student no init no local id.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  public void testLookupStudentNoInitNoLocalID() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", null, null, null));
    when(restUtils.lookupNoInitNoLocalID("19791018", "VANDERLEEK", correlationID)).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoInitNoLocalID("19791018", "VANDERLEEK", correlationID);

    assertNotNull(studentEntities);
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
        .dob(dob)
        .legalLastName(surname)
        .legalFirstName(givenName)
        .mincode(mincode)
        .localID(localID)
        .build();
  }

}
