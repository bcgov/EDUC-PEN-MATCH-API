package ca.bc.gov.educ.api.penmatch.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PenMatchServiceTest {


	PenMatchService service;

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemogRepository;

	@Autowired
	SurnameFrequencyRepository surnameFreqRepository;

	@Before
	public void before() {
		service = new PenMatchService(penDemogRepository, nicknamesRepository, surnameFreqRepository);
	}

	@Test
	public void testMatchStudent_WhenPayloadIsValid_ShouldReturnSavedObject() {
		PenMatchStudent student = createPenMatchStudent();
		assertNotNull(service.matchStudent(student));
		assertNotNull(student.getPenStatus());
	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setAssignmentCode(null);
		student.setAssignmentDate(null);
		student.setEnrolledGradeCode(null);
		student.setFypFlag(null);
		student.setMincode(null);
		student.setPenStatus(null);
		student.setPenStatusMessage(null);
		student.setPostal(null);
		student.setDob(null);
		student.setGivenName(null);
		student.setGivenInitial(null);
		student.setLocalID(null);
		student.setMiddleName(null);
		student.setMiddleInitial(null);
		student.setStudentNumber(null);
		student.setSex(null);
		student.setSurname(null);
		student.setUpdateCode(null);
		student.setUsualGivenName(null);
		student.setUsualGivenInitial(null);
		student.setUsualMiddleName(null);
		student.setUsualMiddleInitial(null);
		student.setUsualSurname(null);
		student.setVersion(null);
		student.setPen1(null);
		student.setPen2(null);
		student.setPen3(null);
		student.setPen4(null);
		student.setPen5(null);
		student.setPen6(null);
		student.setPen7(null);
		student.setPen8(null);
		student.setPen9(null);
		student.setPen10(null);
		student.setPen11(null);
		student.setPen12(null);
		student.setPen13(null);
		student.setPen14(null);
		student.setPen15(null);
		student.setPen16(null);
		student.setPen17(null);
		student.setPen18(null);
		student.setPen19(null);
		student.setPen20(null);

		return student;
	}
}
