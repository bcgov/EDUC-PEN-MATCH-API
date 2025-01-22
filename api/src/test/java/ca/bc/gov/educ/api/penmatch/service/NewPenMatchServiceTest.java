package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.model.v1.FrequencySurnameEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.NicknameEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.v1.match.NewPenMatchService;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchStudentDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junitparams.JUnitParamsRunner;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The type New pen match service test.
 */
@RunWith(JUnitParamsRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class NewPenMatchServiceTest {
  /**
   * The constant scr.
   */
  @ClassRule
  public static final SpringClassRule scr = new SpringClassRule();
  /**
   * The Rest utils.
   */
  @Autowired
  private NewPenMatchService service;
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
   * The Nicknames repository.
   */
  @Autowired
  NicknamesRepository nicknamesRepository;
  /**
   * The Surname freq repository.
   */
  @Autowired
  SurnameFrequencyRepository surnameFreqRepository;
  /**
   * The Rest utils.
   */
  @MockBean
  RestUtils restUtils;

  /**
   * Sets .
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    if (!dataLoaded) {

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

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
  }

  /**
   * Test change result from qto f.
   */
  @Test
  public void testChangeResultFromQtoF() {
    NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
    NewPenMatchSession session = new NewPenMatchSession();
    session.setMatchingRecordsList(new ArrayList<>());
    session.getMatchingRecordsList().add(new NewPenMatchRecord("Q", "1241112", "12445566", "1231232132313123"));

    service.changeResultFromQtoF(student, session);
    assertEquals(0, session.getMatchingRecordsList().size());
  }

  /**
   * Test sum of int match codes.
   */
  @Test
  public void testSumOfIntMatchCodes() {
    assertEquals(10, service.getSumOfMatchCode("1111222"));
  }

  /**
   * Test one char typo true.
   */
  @Test
  public void testOneCharTypoTrue() {
    assertTrue(service.oneCharTypo("MARCO", "MAARCO"));
  }

  /**
   * Test one char typo length false.
   */
  @Test
  public void testOneCharTypoLengthFalse() {
    assertFalse(service.oneCharTypo("MARCO", "MAAARCO"));
  }

  /**
   * Test one char typo false.
   */
  @Test
  public void testOneCharTypoFalse() {
    assertFalse(service.oneCharTypo("MARCO", "JAMES"));
  }

  /**
   * Create new pen match student detail new pen match student detail.
   *
   * @return the new pen match student detail
   */
  private NewPenMatchStudentDetail createNewPenMatchStudentDetail() {
    NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
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

}
