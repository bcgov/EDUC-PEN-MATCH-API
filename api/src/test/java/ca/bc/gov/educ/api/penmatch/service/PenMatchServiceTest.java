package ca.bc.gov.educ.api.penmatch.service;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
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
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PenMatchServiceTest {

	PenMatchService service;

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemogRepository;

	@Autowired
	SurnameFrequencyRepository surnameFreqRepository;

	@Autowired
	PenMatchLookupManager lookupManager;

	@BeforeClass
	public void setup() throws Exception {
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
	}
	
	@Test
	public void testMatchStudent_WhenPayloadIsValidAlg30_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudent();
		student.setPen(null);
		student.setGivenName(null);
		student.setMiddleName("LUKE");
		student.setPostal("V1B1J0");
		student.setSex("M");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudent_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudent();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudent_WhenPayloadIsInValidPEN_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudent();
		student.setPen("123456888");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudent_WhenPayloadIsValidWithInvalidPEN_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudent();
		student.setPen("109508853");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithoutPEN_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithoutPEN();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentNoMatches_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = new PenMatchStudent();
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
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithoutPENNoLocalID_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithoutPEN();
		student.setLocalID(null);
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithFull_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithFullNoSex_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setSex(null);
		student.setDob("19991201");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithFullSplitGiven_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setGivenName("LUKE JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithFullSplitGivenDash_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setGivenName("LUKE-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithUsualSplitGiven_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setUsualGivenName("LUKE JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();
		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	}

	@Test
	public void testMatchStudentWithUsualSmallSurname_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setSurname("LOR");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();
		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
	}

	@Test
	public void testMatchStudentWithFullSplitMiddleDash_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setUsualGivenName("luke-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithMergedRecord_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setUsualGivenName("luke-JACK");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithNoSurname_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchFullStudent();
		student.setSurname(null);
		student.setUsualSurname(null);
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithMergedDeceased_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentMergedDeceased();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithMergedValid_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentMergedValid();

		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}
	
	@Test
	public void testMatchStudentWithMergedValidComplete_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentMergedValidComplete();

		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithRareName_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithRareName();
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithRareNameMiddleFlip_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithRareName();
		student.setMiddleName("VICTORIA");
		student.setGivenName("WILLIAM");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeS_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithRareName();
		student.setUpdateCode("S");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeSWithPostal_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentWithRareName();
		student.setUpdateCode("S");
		student.setPostal("V0B1R0");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	@Test
	public void testMatchStudentWithRareNameWithUpdateCodeY_WhenPayloadIsValid_ShouldReturnSavedObject() throws JsonProcessingException {
		PenMatchStudent student = createPenMatchStudentMergedDeceased();
		student.setUpdateCode("Y");
		PenMatchResult result = service.matchStudent(student);
		assertNotNull(result);
		assertNotNull(result.getPenStatus());
		ObjectMapper mapper = new ObjectMapper();

		log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("122740046");
		student.setSurname("LORD");
		student.setGivenName("CLAYTON");
		student.setMiddleName("L");
		student.setUsualSurname(null);
		student.setUsualGivenName(null);
		student.setUsualMiddleName(null);
		student.setPostal(null);
		student.setDob("19991201");
		student.setLocalID("285261");
		student.setSex("F");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode("00501007");
		student.setEnrolledGradeCode(null);

		return student;
	}

	private PenMatchStudent createPenMatchFullStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("122740046");
		student.setSurname("LORD");
		student.setGivenName("CLAYTON");
		student.setMiddleName("L");
		student.setUsualSurname(null);
		student.setUsualGivenName(null);
		student.setUsualMiddleName(null);
		student.setPostal(null);
		student.setDob("19990112");
		student.setLocalID("285261");
		student.setSex("F");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode("00501007");
		student.setEnrolledGradeCode(null);

		return student;
	}

	private PenMatchStudent createPenMatchStudentWithoutPEN() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen(null);
		student.setSurname("LORD");
		student.setGivenName("CLAYTON");
		student.setMiddleName("L");
		student.setUsualSurname(null);
		student.setUsualGivenName(null);
		student.setUsualMiddleName(null);
		student.setPostal(null);
		student.setDob("19991201");
		student.setLocalID("285261");
		student.setSex("F");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}

	private PenMatchStudent createPenMatchStudentWithRareName() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen(null);
		student.setSurname("ODLUS");
		student.setGivenName("VICTORIA");
		student.setMiddleName("WILLIAM");
		student.setUsualSurname(null);
		student.setUsualGivenName(null);
		student.setUsualMiddleName(null);
		student.setPostal(null);
		student.setDob("19981102");
		student.setLocalID("239661");
		student.setSex("M");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}

	private PenMatchStudent createPenMatchStudentMergedDeceased() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("108999400");
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

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}

	private PenMatchStudent createPenMatchStudentMergedValid() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("113874044");
		student.setSurname("SMITH");
		student.setGivenName("JOE");
		student.setMiddleName("JAMES");
		student.setUsualSurname("SMITH");
		student.setUsualGivenName("JOE");
		student.setUsualMiddleName("JAMES");
		student.setPostal(null);
		student.setDob("19800410");
		student.setLocalID(null);
		student.setSex("M");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}
	
	private PenMatchStudent createPenMatchStudentMergedValidComplete() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("113874044");
		student.setSurname("SMITH");
		student.setGivenName("JOE");
		student.setMiddleName("JAMES");
		student.setUsualSurname("SMITH");
		student.setUsualGivenName("JOE");
		student.setUsualMiddleName("JAMES");
		student.setPostal(null);
		student.setDob("19800412");
		student.setLocalID(null);
		student.setSex("M");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}

}
