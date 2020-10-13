package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.match.NewPenMatchService;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchStudentDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
  private static boolean dataLoaded = false;
  /**
   * The Smr.
   */
  @Rule
  public final SpringMethodRule smr = new SpringMethodRule();
  @Autowired
  NicknamesRepository nicknamesRepository;
  @Autowired
  SurnameFrequencyRepository surnameFreqRepository;
  @MockBean
  RestUtils restUtils;

  @Before
  public void setup() throws Exception {
    if (!dataLoaded) {

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
  }

  @Test
  public void testChangeResultFromQtoF() {
    NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
    NewPenMatchSession session = new NewPenMatchSession();
    session.setMatchingRecordsList(new ArrayList<>());
    session.getMatchingRecordsList().add(new NewPenMatchRecord("Q", "1241112", "12445566", "1231232132313123"));

    service.changeResultFromQtoF(student, session);
    assertEquals(0, session.getMatchingRecordsList().size());
  }

  @Test
  public void testSumOfIntMatchCodes() {
    assertEquals(10, service.getSumOfMatchCode("1111222"));
  }

  @Test
  public void testOneCharTypoTrue() {
    assertTrue(service.oneCharTypo("MARCO", "MAARCO"));
  }

  @Test
  public void testOneCharTypoLengthFalse() {
    assertFalse(service.oneCharTypo("MARCO", "MAAARCO"));
  }

  @Test
  public void testOneCharTypoFalse() {
    assertFalse(service.oneCharTypo("MARCO", "JAMES"));
  }

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
