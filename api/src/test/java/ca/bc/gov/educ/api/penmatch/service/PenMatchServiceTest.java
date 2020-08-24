package ca.bc.gov.educ.api.penmatch.service;

import java.io.File;
import java.util.List;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudentDetail;
import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PenMatchServiceTest {

	private static PenMatchService service;

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemogRepository;

	@Autowired
	SurnameFrequencyRepository surnameFreqRepository;

	@Autowired
	PenMatchLookupManager lookupManager;

	private static boolean dataLoaded = false;

	@Before
	public void setup() throws Exception {
		if (!dataLoaded) {
			service = new PenMatchService(lookupManager);
			final File file = new File("src/test/resources/mock_pen_demog.json");
			List<PenDemographicsEntity> penDemogEntities = new ObjectMapper().readValue(file, new TypeReference<List<PenDemographicsEntity>>() {
			});
			penDemogRepository.saveAll(penDemogEntities);

			final File fileNick = new File("src/test/resources/mock_nicknames.json");
			List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<List<NicknamesEntity>>() {
			});
			nicknamesRepository.saveAll(nicknameEntities);

			final File fileSurnameFreqs = new File("src/test/resources/mock_surname_frequency.json");
			List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFreqs, new TypeReference<List<SurnameFrequencyEntity>>() {
			});
			surnameFreqRepository.saveAll(surnameFreqEntities);
			dataLoaded = true;
		}
	}

	@Test
	public void testMatchStudent_Alg30_ShouldReturnStatusD1Match()  {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen(null);
		student.setGivenName(null);
		student.setMiddleName("LUKE");
		student.setPostal("V1B1J0");
		student.setSex("M");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudent_Alg40_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen(null);
		student.setGivenName(null);
		student.setMiddleName(null);
		student.setSex("F");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudent_Alg50_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen(null);
		student.setGivenName(null);
		student.setMiddleName(null);
		student.setSex("M");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudent_WhenPayloadIsValidAlg51_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen(null);
		student.setGivenName("CLA");
		student.setMiddleName(null);
		student.setSex("F");
		student.setDob("19990501");
		student.setLocalID(null);
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudent_ShouldReturnAAMatch() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.AA.toString());
	}

	@Test
	public void testMatchStudentForCoreCheck_ShouldReturnC0() {
		PenMatchStudentDetail student = createPenMatchStudentDetailForCoreCheck();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertEquals(result.getPenStatus(), PenStatus.C0.toString());
	}

	@Test
	public void testMatchStudentValidTwin_ShouldReturnAA() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setLocalID("285262");
		student.setGivenName("CLAYTON");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.AA.toString());
	}

	@Test
	public void testMatchStudentInValidPEN_ShouldReturnC1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen("123456888");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.C1.toString());
	}

	@Test
	public void testMatchStudentWithInvalidPEN_ShouldReturnC1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetail();
		student.setPen("109508853");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.C1.toString());
	}

	@Test
	public void testMatchStudentWithoutPEN_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithoutPEN();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

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
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertEquals(result.getPenStatus(), PenStatus.D0.toString());
	}

	@Test
	public void testMatchStudentWithoutPENNoLocalID_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithoutPEN();
		student.setLocalID(null);
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithFull_ShouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithFullNoSex_ShouldReturnAAMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setSex(null);
		student.setDob("19991201");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.AA.toString());
	}

	@Test
	public void testMatchStudentWithFullSplitGiven_ShouldReturnF1() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setGivenName("LUKE JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithFullSplitGivenDash_ShouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setGivenName("LUKE-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithUsualSplitGiven_shouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setUsualGivenName("LUKE JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithUsualSmallSurname_ShouldReturnF1() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setSurname("LOR");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithFullSplitMiddleDash_ShouldReturnF1andPossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setUsualGivenName("luke-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithMergedRecord_ShouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setUsualGivenName("luke-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithNoSurname_ShouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchFullStudent();
		student.setSurname(null);
		student.setUsualSurname(null);
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithMergedDeceased_ShouldReturnC0() {
		PenMatchStudentDetail student = createPenMatchStudentDetailMergedDeceased();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertEquals(result.getPenStatus(), PenStatus.C0.toString());
	}

	@Test
	public void testMatchStudentWithMergedValid_ShouldReturnF1PossibleMatch() {
		PenMatchStudentDetail student = createPenMatchStudentDetailMergedValid();

		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertTrue(result.getMatchingRecords().size() > 0);
		assertEquals(result.getPenStatus(), PenStatus.F1.toString());
	}

	@Test
	public void testMatchStudentWithMergedValidComplete_ShouldReturnB1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailMergedValidComplete();

		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.B1.toString());
	}

	@Test
	public void testMatchStudentWithRareName_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameMiddleFlip_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setMiddleName("VICTORIA");
		student.setGivenName("WILLIAM");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeS_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setMincode("00501007");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWrongGiven_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setGivenName("PETE");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWrongMiddle_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setMiddleName("YARN");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWrongLocalID_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setLocalID("239661");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());

	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWrongSurname_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setSurname("JAKE");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWrongSex_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setSex("F");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWithWrongPostal_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setPostal("V0B1R2");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWithWrongDob_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setDob("19920223");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSValid_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWithPostal_ShouldReturnD1Match() {
		PenMatchStudentDetail student = createPenMatchStudentDetailWithRareName();
		student.setUpdateCode("S");
		student.setPostal("V0B1R0");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertNotNull(result.getPen());
		assertEquals(result.getPenStatus(), PenStatus.D1.toString());
	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeY_ShouldReturnC0() {
		PenMatchStudentDetail student = createPenMatchStudentDetailMergedDeceased();
		student.setUpdateCode("Y");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		assertEquals(result.getPenStatus(), PenStatus.C0.toString());
	}

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

	private PenMatchStudentDetail createPenMatchStudentDetailWithRareName() {
		PenMatchStudentDetail student = new PenMatchStudentDetail();
		student.setSurname("ODLUS");
		student.setGivenName("VICTORIA");
		student.setMiddleName("WILLIAM");
		student.setDob("19981102");
		student.setLocalID("239661");
		student.setSex("M");

		return student;
	}

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

	private PenMatchStudentDetail createPenMatchStudentDetailMergedValidComplete() {
		PenMatchStudentDetail student = new PenMatchStudentDetail();
		student.setPen("113874044");
		student.setSurname("SMITH");
		student.setGivenName("JOE");
		student.setMiddleName("JAMES");
		student.setUsualSurname("SMITH");
		student.setUsualGivenName("JOE");
		student.setUsualMiddleName("JAMES");
		student.setDob("19800412");
		student.setSex("M");

		return student;
	}

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

}
