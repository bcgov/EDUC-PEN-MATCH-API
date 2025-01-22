package ca.bc.gov.educ.api.penmatch.util;

import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
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

/**
 * The type Pen match utils test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchUtilsTest {

  /**
   * Before.
   */
  @Before
	public void before() {
	}

  /**
   * Test set nicknames should return contain names.
   */
  @Test
	public void testSetNicknames_ShouldReturnContainNames() {

		PenMatchNames penMatchTransactionNames = new PenMatchNames();
	  	penMatchTransactionNames.getNicknames().add("Wayne");
		assertEquals(1, penMatchTransactionNames.getNicknames().size());

        penMatchTransactionNames.getNicknames().add("Wayner");
	    assertEquals(2, penMatchTransactionNames.getNicknames().size());

		penMatchTransactionNames.getNicknames().add("Wayners");
		assertEquals(3, penMatchTransactionNames.getNicknames().size());

	    penMatchTransactionNames.getNicknames().add("Way");
	    assertEquals(4, penMatchTransactionNames.getNicknames().size());
	}

  /**
   * Test check for core data should not set status.
   */
  @Test
	public void testCheckForCoreData_ShouldNotSetStatus() {
		PenMatchStudent student = createPenMatchStudent();
		PenMatchSession session = new PenMatchSession();
		PenMatchUtils.checkForCoreData(student, session);

		assertNull(session.getPenStatus());
	}

  /**
   * Test check for core data should set g 0 status.
   */
  @Test
	public void testCheckForCoreData_ShouldSetG0Status() {
		PenMatchStudent student = createPenMatchStudent();
		PenMatchSession session = new PenMatchSession();
		student.setSurname(null);
		PenMatchUtils.checkForCoreData(student, session);

    assertEquals(session.getPenStatus(), PenStatus.G0.getValue());
	}

  /**
   * Test normalize local i ds from master should set mmm.
   */
  @Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetMMM() {
		PenMasterRecord master = new PenMasterRecord();
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

    assertEquals("MMM", master.getAlternateLocalId());
	}

  /**
   * Test normalize local i ds from master should set local id.
   */
  @Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetLocalID() {
		PenMasterRecord master = new PenMasterRecord();
		master.setLocalId("123456");
		PenMatchUtils.normalizeLocalIDsFromMaster(master);

    assertEquals(master.getAlternateLocalId(), master.getLocalId());
	}

  /**
   * Test store names from master should create match master names.
   */
  @Test
	public void testStoreNamesFromMaster_ShouldCreateMatchMasterNames() {
		assertNotNull(PenMatchUtils.storeNamesFromMaster(createPenMasterRecord()));
	}

  /**
   * Test store names from master with space should create match master names.
   */
  @Test
	public void testStoreNamesFromMasterWithSpace_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setGiven("Billy Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

  /**
   * Test store names from master with dash should create match master names.
   */
  @Test
	public void testStoreNamesFromMasterWithDash_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setGiven("Billy-Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

  /**
   * Test usual store names from master with space should create match master names.
   */
  @Test
	public void testUsualStoreNamesFromMasterWithSpace_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setUsualGivenName("Billy Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

  /**
   * Test usual store names from master with dash should create match master names.
   */
  @Test
	public void testUsualStoreNamesFromMasterWithDash_ShouldCreateMatchMasterNames() {
		PenMasterRecord master = createPenMasterRecord();
		master.setUsualGivenName("Billy-Joe");
		assertNotNull(PenMatchUtils.storeNamesFromMaster(master));
	}

  /**
   * Test convert to pen master record should assert ok.
   */
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

  /**
   * Create pen master record pen master record.
   *
   * @return the pen master record
   */
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

  /**
   * Create pen match student pen match student.
   *
   * @return the pen match student
   */
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
