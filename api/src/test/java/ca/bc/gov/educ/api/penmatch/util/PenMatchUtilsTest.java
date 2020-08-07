package ca.bc.gov.educ.api.penmatch.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PenMatchUtilsTest {

	@Before
	public void before() {
	}

	@Test
	public void testSetNicknames_ShouldReturnContainNames() {
		PenMatchUtils utils = new PenMatchUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		utils.setNextNickname(penMatchTransactionNames, "Wayne");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNull(penMatchTransactionNames.getNickname2());

		utils.setNextNickname(penMatchTransactionNames, "Wayner");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNull(penMatchTransactionNames.getNickname3());

		utils.setNextNickname(penMatchTransactionNames, "Wayners");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNotNull(penMatchTransactionNames.getNickname3());
		assertNull(penMatchTransactionNames.getNickname4());

		utils.setNextNickname(penMatchTransactionNames, "Way");
		assertNotNull(penMatchTransactionNames.getNickname1());
		assertNotNull(penMatchTransactionNames.getNickname2());
		assertNotNull(penMatchTransactionNames.getNickname3());
		assertNotNull(penMatchTransactionNames.getNickname4());
	}

	@Test
	public void testEmptyNicknameListHasNameAsGiven_NotFound() {
		PenMatchUtils utils = new PenMatchUtils();

		List<NicknamesEntity> nicknameEntityList = new ArrayList<NicknamesEntity>();

		assertFalse(utils.hasGivenNameAsNickname2(nicknameEntityList, "Wayne"));
	}

	@Test
	public void testNicknameListHasNameAsGiven_Found() {
		PenMatchUtils utils = new PenMatchUtils();

		List<NicknamesEntity> nicknameEntityList = new ArrayList<NicknamesEntity>();

		NicknamesEntity entity = new NicknamesEntity("Wayner", "Wayne");
		nicknameEntityList.add(entity);

		assertTrue(utils.hasGivenNameAsNickname2(nicknameEntityList, "Wayne"));
	}

	@Test
	public void testNicknameListHasNameAsGiven_NotFound() {
		PenMatchUtils utils = new PenMatchUtils();

		List<NicknamesEntity> nicknameEntityList = new ArrayList<NicknamesEntity>();

		NicknamesEntity entity = new NicknamesEntity("Peter", "Pete");
		nicknameEntityList.add(entity);

		assertFalse(utils.hasGivenNameAsNickname2(nicknameEntityList, "Wayne"));
	}

	@Test
	public void testCheckForCoreData_ShouldNotSetStatus() {
		PenMatchUtils utils = new PenMatchUtils();

		PenMatchStudent student = createPenMatchStudent();
		utils.checkForCoreData(student);

		assertNull(student.getPenStatus());
	}

	@Test
	public void testCheckForCoreData_ShouldSetG0Status() {
		PenMatchUtils utils = new PenMatchUtils();

		PenMatchStudent student = createPenMatchStudent();
		student.setSurname(null);
		utils.checkForCoreData(student);

		assertTrue(student.getPenStatus().equals(PenStatus.G0.getValue()));
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetMMM() {
		PenMatchUtils utils = new PenMatchUtils();

		PenMasterRecord master = new PenMasterRecord();
		utils.normalizeLocalIDsFromMaster(master);

		assertTrue(master.getAlternateLocalId().equals("MMM"));
	}

	@Test
	public void testNormalizeLocalIDsFromMaster_ShouldSetLocalID() {
		PenMatchUtils utils = new PenMatchUtils();

		PenMasterRecord master = new PenMasterRecord();
		master.setLocalId("123456");
		utils.normalizeLocalIDsFromMaster(master);

		assertTrue(master.getAlternateLocalId().equals(master.getLocalId()));
	}

	@Test
	public void testConvertToPenMasterRecord_ShouldAssertOk() {
		PenMatchUtils utils = new PenMatchUtils();
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
		entity.setGrade("10");
		entity.setStudStatus("B0");
		entity.setMincode("12345678");
		entity.setLocalID("9876575");

		PenMasterRecord masterRecord = utils.convertPenDemogToPenMasterRecord(entity);

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
		assertNotNull(masterRecord.getGrade());
		assertNotNull(masterRecord.getStatus());
		assertNotNull(masterRecord.getMincode());
		assertNotNull(masterRecord.getLocalId());
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
