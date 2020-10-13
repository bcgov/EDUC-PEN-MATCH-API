package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.*;

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

    assertEquals(session.getPenStatus(), PenStatus.G0.getValue());
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetMMM() {
		PenMasterRecord master = new PenMasterRecord();
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

    assertEquals("MMM", master.getAlternateLocalId());
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetLocalID() {
		PenMasterRecord master = new PenMasterRecord();
		master.setLocalId("123456");
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

    assertEquals(master.getAlternateLocalId(), master.getLocalId());
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

		StudentEntity entity = new StudentEntity();
		entity.setPen("123456789");
		entity.setDob("1980-01-15");
		entity.setLegalLastName("JACKSON");
		entity.setLegalFirstName("PETER");
		entity.setLegalMiddleNames("AXLE");
		entity.setUsualLastName("JACKSON");
		entity.setUsualFirstName("PETE");
		entity.setUsualMiddleNames("AXE");
		entity.setPostalCode("V2R3W4");
		entity.setSexCode("M");
		entity.setStatusCode("B0");
		entity.setMincode("12345678");
		entity.setLocalID("9876575");
		entity.setStudentID(UUID.randomUUID());

		PenMasterRecord masterRecord = PenMatchUtils.convertStudentEntityToPenMasterRecord(entity);
		assertNotNull(masterRecord.getPen());
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

		masterRecord.setPen("12345647");
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

		student.setUpdateCode(null);
		student.setEnrolledGradeCode(null);

		return student;
	}
}
