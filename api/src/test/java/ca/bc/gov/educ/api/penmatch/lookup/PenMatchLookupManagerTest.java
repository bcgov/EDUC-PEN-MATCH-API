package ca.bc.gov.educ.api.penmatch.lookup;

import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.repository.ForeignSurnameRepository;
import ca.bc.gov.educ.api.penmatch.repository.MatchCodesRepository;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchLookupManagerTest {
  private static PenMatchLookupManager lookupManager;
  private static boolean dataLoaded = false;
  @Autowired
  RestUtils restUtils;
  @MockBean
  RestTemplate restTemplate;

  @Autowired
  ApplicationProperties props;
  @Autowired
  ForeignSurnameRepository foreignSurnameRepository;
  @Autowired
  NicknamesRepository nicknamesRepository;
  @Autowired
  MatchCodesRepository matchCodesRepository;
  @Autowired
  SurnameFrequencyRepository surnameFrequencyRepository;

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
      lookupManager = new PenMatchLookupManager(foreignSurnameRepository, nicknamesRepository, surnameFrequencyRepository, matchCodesRepository, restUtils, props);
      dataLoaded = true;
    }
    when(restUtils.getRestTemplate()).thenReturn(restTemplate);
  }

  @Test
  public void testLookupSurnameFrequency_ShouldReturn0() {
    assertEquals(0, (int) lookupManager.lookupSurnameFrequency("ASDFJSD"));
  }

  @Test
  public void testLookupSurnameFrequency_ShouldReturnOver200() {
    assertTrue(lookupManager.lookupSurnameFrequency("JAM") > 200);
  }

  @Test
  public void testLookupNicknames_ShouldReturn4Names() {
    PenMatchNames penMatchTransactionNames = new PenMatchNames();

    lookupManager.lookupNicknames(penMatchTransactionNames, "JAMES");

    assertNotNull(penMatchTransactionNames.getNickname1());
    assertNotNull(penMatchTransactionNames.getNickname2());
    assertNotNull(penMatchTransactionNames.getNickname3());
    assertNotNull(penMatchTransactionNames.getNickname4());
  }

  @Test
  public void testLookupStudentByPEN() {
    when(restUtils.getPenMasterRecordByPen("108999400")).thenReturn(Optional.of(new PenMasterRecord()));
    var masterRecord = lookupManager.lookupStudentByPEN("108999400");

    assertThat(masterRecord).isPresent();
  }

  @Test
  public void testLookupStudentWithAllParts() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", "VICTORIA", "00501007", "239661"));
    when(restUtils.lookupWithAllParts("19981102", "ODLUS", "VICTORIA", "00501007", "239661")).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupWithAllParts("19981102", "ODLUS", "VICTORIA", "00501007", "239661");

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  @Test
  public void testLookupStudentNoInitLargeData() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", null, "VICTORIA", "00501007"));
    when(restUtils.lookupNoInit("19981102", "ODLUS", "VICTORIA", "00501007")).thenReturn(students);

    List<StudentEntity> studentEntities = lookupManager.lookupNoInit("19981102", "ODLUS", "VICTORIA", "00501007");
    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  @Test
  public void testLookupStudentNoInit() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", null, "JAKE", "08288006"));
    when(restUtils.lookupNoInit("19791018", "VANDERLEEK", "JAKE", "08288006")).thenReturn(students);

    List<StudentEntity> studentEntities = lookupManager.lookupNoInit("19791018", "VANDERLEEK", "JAKE", "08288006");

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  @Test
  public void testLookupStudentNoLocalIDLargeData() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19981102", "ODLUS", "VICTORIA", null, null));
    when(restUtils.lookupNoLocalID("19981102", "ODLUS", "VICTORIA")).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoLocalID("19981102", "ODLUS", "VICTORIA");

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  @Test
  public void testLookupStudentNoLocalID() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", "JAKE", null, null));
    when(restUtils.lookupNoLocalID("19791018", "VANDERLEEK", "JAKE")).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoLocalID("19791018", "VANDERLEEK", "JAKE");

    assertNotNull(studentEntities);
    assertTrue(studentEntities.size() > 0);
  }

  @Test
  public void testLookupStudentNoInitNoLocalID() throws JsonProcessingException {
    var students = new ArrayList<StudentEntity>();
    students.add(createStudent("19791018", "VANDERLEEK", null, null, null));
    when(restUtils.lookupNoInitNoLocalID("19791018", "VANDERLEEK")).thenReturn(students);
    List<StudentEntity> studentEntities = lookupManager.lookupNoInitNoLocalID("19791018", "VANDERLEEK");

    assertNotNull(studentEntities);
  }

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
