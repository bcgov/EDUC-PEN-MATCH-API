package ca.bc.gov.educ.api.penmatch.util;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.struct.LocalIDMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ScoringUtilsTest {

	@Before
	public void before() {
	}

	@Test
	public void testMatchAddress_ShouldScore0() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();

		assertTrue(utils.matchAddress(student, master) == 0);
	}

	@Test
	public void testMatchAddressRural_ShouldScore1() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setPostal("V0R3W5");
		master.setPostal("V0R3W5");

		assertTrue(utils.matchAddress(student, master) == 1);
	}

	@Test
	public void testMatchAddressValid_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setPostal("V1R3W5");
		master.setPostal("V1R3W5");

		assertTrue(utils.matchAddress(student, master) == 10);
	}

	@Test
	public void testMatchBirthday_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("19800518");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 20);
	}

	@Test
	public void testMatchBirthdayMonthDayFlip_ShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("19801805");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 15);
	}

	@Test
	public void testMatchBirthday5outOf6_ShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("20100518");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 15);
	}

	@Test
	public void testMatchBirthdaySameYearMonth_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("19800510");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 10);
	}

	@Test
	public void testMatchBirthdaySameYearDay_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("19801018");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 10);
	}

	@Test
	public void testMatchBirthdaySameMonthDay_ShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("20010518");
		master.setDob("19800518");

		assertTrue(utils.matchBirthday(student, master) == 5);
	}

	@Test
	public void testMatchBirthdaySameYear_ShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob("19801018");
		master.setDob("19800519");

		assertTrue(utils.matchBirthday(student, master) == 5);
	}
	
	@Test
	public void testMatchLocalID_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("123456789");
		student.setMincode("987654321");
		master.setLocalId("123456789");
		master.setMincode("987654321");
		master.setAlternateLocalId("123456789");
		
		PenMatchSession session = new PenMatchSession();
		session.setAlternateLocalID("123456789");

		assertTrue(utils.matchLocalID(student, master, session).getLocalIDPoints() == 20);
	}
	
	@Test
	public void testMatchLocalIDSameSchool_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("123456789");
		student.setMincode("987654321");
		master.setLocalId("123456788");
		master.setMincode("987654321");
		
		PenMatchSession session = new PenMatchSession();

		assertTrue(utils.matchLocalID(student, master, session).getLocalIDPoints() == 10);
	}
	
	@Test
	public void testMatchLocalIDSameDistrict_ShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("12388888");
		student.setMincode("987654321");
		master.setLocalId("123456788");
		master.setMincode("987884321");
		
		PenMatchSession session = new PenMatchSession();

		assertTrue(utils.matchLocalID(student, master, session).getLocalIDPoints() == 5);
	}
	
	@Test
	public void testMatchLocalIDSameDistrict102_ShouldScore0() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("12388888");
		student.setMincode("102654321");
		master.setLocalId("123456788");
		master.setMincode("102884321");
		
		PenMatchSession session = new PenMatchSession();

		assertTrue(utils.matchLocalID(student, master, session).getLocalIDPoints() == 0);
	}
	
	@Test
	public void testMatchLocalIDWithDemerits_ShouldScore10Demerits() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("123456789");
		student.setMincode("123456788");
		master.setLocalId("123456788");
		master.setMincode("123456788");
		
		PenMatchSession session = new PenMatchSession();

		LocalIDMatchResult result = utils.matchLocalID(student, master, session);
		assertTrue(result.getIdDemerits() == 10);
		assertTrue(result.getLocalIDPoints() == 10);
	}

	public PenMasterRecord createPenMasterRecord() {
		PenMasterRecord masterRecord = new PenMasterRecord();

		masterRecord.setStudentNumber("12345647");
		masterRecord.setDob("19800518");
		masterRecord.setSurname("JACKSON");
		masterRecord.setGiven("PETER");
		masterRecord.setMiddle(null);
		masterRecord.setUsualSurname(null);
		masterRecord.setUsualGivenName(null);
		masterRecord.setUsualMiddleName(null);
		masterRecord.setPostal(null);
		masterRecord.setSex("M");
		masterRecord.setGrade("10");
		masterRecord.setStatus(PenStatus.AA.getValue());
		masterRecord.setMincode("123456978");
		masterRecord.setLocalId(null);

		return masterRecord;
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
		student.setMincode("12345567");

		student.setUsualMiddleInitial(null);
		student.setUsualGivenInitial(null);

		student.setGivenInitial(null);
		student.setMiddleInitial(null);
		student.setUpdateCode(null);

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
