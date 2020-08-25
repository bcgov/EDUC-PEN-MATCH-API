package ca.bc.gov.educ.api.penmatch.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchUtilsTest {

	@Before
	public void before() {
	}

	@Test
	public void testSetNicknames_ShouldReturnContainNames() {

		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchUtils.setNextNickname(penMatchTransactionNames, "Wayne");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNull(penMatchTransactionNames.getNickname2());

		PenMatchUtils.setNextNickname(penMatchTransactionNames, "Wayner");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNull(penMatchTransactionNames.getNickname3());

		PenMatchUtils.setNextNickname(penMatchTransactionNames, "Wayners");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNotNull(penMatchTransactionNames.getNickname3());
		assertNull(penMatchTransactionNames.getNickname4());

		penMatchTransactionNames.setNickname4("");
		PenMatchUtils.setNextNickname(penMatchTransactionNames, "Way");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNotNull(penMatchTransactionNames.getNickname3());
		assertNotNull(penMatchTransactionNames.getNickname4());
	}

	@Test
	public void testCheckForCoreData_ShouldNotSetStatus() {
		PenMatchStudent student = createPenMatchStudent();
		PenMatchSession session = new PenMatchSession();
		PenMatchUtils.checkForCoreData(student, session);

		assertNull(session.getPenStatus());
	}

	@Test
	public void testCheckForCoreData_ShouldSetG0Status() {
		PenMatchStudent student = createPenMatchStudent();
		PenMatchSession session = new PenMatchSession();
		student.setSurname(null);
		PenMatchUtils.checkForCoreData(student, session);

		assertTrue(session.getPenStatus().equals(PenStatus.G0.getValue()));
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetMMM() {
		PenMasterRecord master = new PenMasterRecord();
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

		assertTrue(master.getAlternateLocalId().equals("MMM"));
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetLocalID() {
		PenMasterRecord master = new PenMasterRecord();
		master.setLocalId("123456");
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

		assertTrue(master.getAlternateLocalId().equals(master.getLocalId()));
	}

	@Test
	public void testStoreNamesFromMaster_ShouldCreateMatchMasterNames() {
		assertNotNull(PenMatchUtils.storeNamesFromMaster(createPenMasterRecord()));
	}

	@Test
	public void testStoreNamesFromMasterWithSpace_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setGiven("Billy Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

	@Test
	public void testStoreNamesFromMasterWithDash_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setGiven("Billy-Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

	@Test
	public void testUsualStoreNamesFromMasterWithSpace_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setUsualGivenName("Billy Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

	@Test
	public void testUsualStoreNamesFromMasterWithDash_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setUsualGivenName("Billy-Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

	@Test
	public void testConvertToPenMasterRecord_ShouldAssertOk() {

		PenDemographicsEntity entity = new PenDemographicsEntity();
		entity.setStudNo("123456789");
		entity.setStudBirth("19800115");
		entity.setStudSurname("JACKSON");
		entity.setStudGiven("PETER");
		entity.setStudMiddle("AXLE");
		entity.setUsualSurname("JACKSON");
		entity.setUsualGiven("PETE");
		entity.setUsualMiddle("AXE");
		entity.setPostalCode("V2R3W4");
		entity.setStudSex("M");
		entity.setStudStatus("B0");
		entity.setMincode("12345678");
		entity.setLocalID("9876575");

		PenMasterRecord masterRecord = PenMatchUtils.convertPenDemogToPenMasterRecord(entity);
		assertNotNull(masterRecord.getStudentNumber());
		assertNotNull(masterRecord.getDob());
		assertNotNull(masterRecord.getSurname());
		assertNotNull(masterRecord.getGiven());
		assertNotNull(masterRecord.getMiddle());
		assertNotNull(masterRecord.getUsualSurname());
		assertNotNull(masterRecord.getUsualGivenName());
		assertNotNull(masterRecord.getUsualMiddleName());
		assertNotNull(masterRecord.getPostal());
		assertNotNull(masterRecord.getSex());
		assertNotNull(masterRecord.getStatus());
		assertNotNull(masterRecord.getMincode());
		assertNotNull(masterRecord.getLocalId());
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
		student.setPen(null);
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

		student.setUsualMiddleInitial("G");
		student.setUsualGivenInitial("J");

		student.setGivenInitial("G");
		student.setMiddleInitial("J");
		student.setUpdateCode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}
}
