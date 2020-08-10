package ca.bc.gov.educ.api.penmatch.lookup;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import ca.bc.gov.educ.api.penmatch.util.ScoringUtils;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PenMatchLookupManagerTest {

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemographicsRepository;

	@Autowired
	SurnameFrequencyRepository surnameFrequencyRepository;

	@Autowired
	private EntityManager entityManager;

	PenMatchLookupManager lookupManager;

	@Before
	public void before() {
		lookupManager = new PenMatchLookupManager(entityManager, penDemographicsRepository, nicknamesRepository, surnameFrequencyRepository);
	}

	@Test
	public void testMatchStudent_WhenPayloadIsValid_ShouldReturnSavedObject() {
		PenMatchStudent student = createPenMatchStudent();
		assertTrue(lookupManager.lookupSurnameFrequency("Micheals") == 20);
	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setStudentNumber(null);
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

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);
		student.setMincode(null);
		student.setAssignmentCode(null);
		student.setAssignmentDate(null);
		student.setEnrolledGradeCode(null);
		student.setFypFlag(null);
		student.setPenStatus(null);
		student.setPenStatusMessage(null);
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
