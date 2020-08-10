package ca.bc.gov.educ.api.penmatch.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.struct.GivenNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.LocalIDMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.MiddleNameMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.SurnameMatchResult;

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
	public void testMatchBirthday_NoDob() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setDob(null);
		master.setDob(null);

		assertNull(utils.matchBirthday(student, master));
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
	public void testMatchLocalID_AlternateLocalIdScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("123456789");
		student.setMincode("987654321");
		master.setMincode("987654321");
		master.setAlternateLocalId("123456789");

		PenMatchSession session = new PenMatchSession();
		session.setAlternateLocalID("123456789");

		assertTrue(utils.matchLocalID(student, master, session).getLocalIDPoints() == 20);
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

	@Test
	public void testMatchLocalIDWithDemerits_ShouldScore10DemeritsWithAlternate() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setLocalID("123456789");
		student.setMincode("987654321");
		master.setMincode("987654321");
		master.setAlternateLocalId("123456788");

		PenMatchSession session = new PenMatchSession();
		session.setAlternateLocalID("123456789");

		LocalIDMatchResult result = utils.matchLocalID(student, master, session);
		assertTrue(result.getIdDemerits() == 10);
		assertTrue(result.getLocalIDPoints() == 10);
	}

	@Test
	public void testMatchSex_ShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSex("M");
		master.setSex("M");

		assertTrue(utils.matchSex(student, master) == 5);
	}

	@Test
	public void testMatchGivenNameLegal_FullShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("MichealsJ");
		penMatchMasterNames.setLegalGiven("MichealsJ");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_FullShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("MichealsJ");
		penMatchMasterNames.setUsualGiven("MichealsJ");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_AlternateLegalShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("MichealsJ");
		penMatchMasterNames.setAlternateLegalGiven("MichealsJ");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_AlternateUsualShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("MichealsJ");
		penMatchMasterNames.setAlternateUsualGiven("MichealsJ");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameLegal_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("Michs");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("Michs");
		penMatchMasterNames.setUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateUsual_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("Michs");
		penMatchMasterNames.setAlternateUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegal_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michs");
		penMatchMasterNames.setAlternateLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameLegal_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("Marcs");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 5);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("Marcs");
		penMatchMasterNames.setUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 5);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateUsual_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("Marcs");
		penMatchMasterNames.setAlternateUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 5);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegal_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Marcs");
		penMatchMasterNames.setAlternateLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 5);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameLegal_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("Michealalas");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("Michealalas");
		penMatchMasterNames.setUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateUsual_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("Michealalas");
		penMatchMasterNames.setAlternateUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegal_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michealalas");
		penMatchMasterNames.setAlternateLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 20);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameLegal_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("alalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsual_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("alalad");
		penMatchMasterNames.setUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateUsual_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("alalad");
		penMatchMasterNames.setAlternateUsualGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegal_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("alalad");
		penMatchMasterNames.setAlternateLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 15);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameLegalToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalGiven("Michealalad");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameUsualToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualGiven("Michealalad");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateUsualToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualGiven("Michealalad");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegalToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegalToUsualGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
		penMatchMasterNames.setUsualMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegalToAltLegalGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
		penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameAlternateLegalToAltUsualGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalGiven("Michealalad");
		penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertTrue(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameNickname1_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setNickname1("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameNickname2_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setNickname2("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameNickname3_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setNickname3("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchGivenNameNickname4_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setNickname4("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		GivenNameMatchResult result = utils.matchGivenName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getGivenNamePoints() == 10);
		assertFalse(result.isGivenNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegal_FullShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("MichealsJ");
		penMatchMasterNames.setLegalMiddle("MichealsJ");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_FullShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("MichealsJ");
		penMatchMasterNames.setUsualMiddle("MichealsJ");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_AlternateLegalShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("MichealsJ");
		penMatchMasterNames.setAlternateLegalMiddle("MichealsJ");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_AlternateUsualShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("MichealsJ");
		penMatchMasterNames.setAlternateUsualMiddle("MichealsJ");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegal_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("Michs");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("Michs");
		penMatchMasterNames.setUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateUsual_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("Michs");
		penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegal_4CharShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michs");
		penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegal_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("Marcs");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 5);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("Marcs");
		penMatchMasterNames.setUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 5);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateUsual_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("Marcs");
		penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 5);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegal_1CharShouldScore5() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Marcs");
		penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 5);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegal_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("Michealalas");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("Michealalas");
		penMatchMasterNames.setUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateUsual_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("Michealalas");
		penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegal_10CharShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michealalas");
		penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 20);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegal_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("alalad");
		penMatchMasterNames.setLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsual_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("alalad");
		penMatchMasterNames.setUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateUsual_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("alalad");
		penMatchMasterNames.setAlternateUsualMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegal_SubsetShouldScore15() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("alalad");
		penMatchMasterNames.setAlternateLegalMiddle("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 15);
		assertFalse(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameLegalToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setLegalMiddle("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameUsualToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setUsualMiddle("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateUsualToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateUsualMiddle("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegalToGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
		penMatchMasterNames.setLegalGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegalToUsualGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
		penMatchMasterNames.setUsualGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegalToAltLegalGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
		penMatchMasterNames.setAlternateLegalGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchMiddleNameAlternateLegalToAltUsualGiven_SubsetShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchNames penMatchTransactionNames = new PenMatchNames();
		PenMatchNames penMatchMasterNames = new PenMatchNames();
		penMatchTransactionNames.setAlternateLegalMiddle("Michealalad");
		penMatchMasterNames.setAlternateUsualGiven("Michealalad");
		MiddleNameMatchResult result = utils.matchMiddleName(penMatchTransactionNames, penMatchMasterNames);
		assertTrue(result.getMiddleNamePoints() == 10);
		assertTrue(result.isMiddleNameFlip());
	}

	@Test
	public void testMatchSurnameLegal_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Micheals");
		master.setSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 20);
		assertTrue(result.isLegalSurnameUsed());
	}

	@Test
	public void testMatchSurnameUsual_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		master.setSurname(null);
		student.setUsualSurname("Micheals");
		master.setUsualSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 20);
		assertFalse(result.isLegalSurnameUsed());
	}

	@Test
	public void testMatchSurnameLegalToUsual_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Micheals");
		student.setUsualSurname(null);
		master.setSurname(null);
		master.setUsualSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 20);
		assertTrue(result.isLegalSurnameUsed());
	}

	@Test
	public void testMatchSurnameUsualToLegal_ShouldScore20() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		student.setUsualSurname("Micheals");
		master.setSurname("Micheals");
		master.setUsualSurname(null);
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 20);
		assertFalse(result.isLegalSurnameUsed());
	}

	@Test
	public void testMatchSurnameLegal4Char_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Michells");
		master.setSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchSurnameUsualToLegal4Char_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Michells");
		master.setSurname(null);
		student.setUsualSurname(null);
		master.setUsualSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchSurnameLegalToUsual4Char_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		master.setSurname("Michells");
		student.setUsualSurname("Micheals");
		master.setUsualSurname(null);
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchSurnameUsualToUsual4Char_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		master.setSurname(null);
		student.setUsualSurname("Michichy");
		master.setUsualSurname("Michells");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchLegalSurnameSoundex_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Micheals");
		master.setSurname("Micells");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchUsualSurnameSoundex_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		master.setSurname(null);
		student.setUsualSurname("Micells");
		master.setUsualSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchUsualToLegalSurnameSoundex_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname("Micells");
		master.setSurname(null);
		student.setUsualSurname(null);
		master.setUsualSurname("Micheals");
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchLegalToUsualSurnameSoundex_ShouldScore10() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSurname(null);
		master.setSurname("Micells");
		student.setUsualSurname("Micheals");
		master.setUsualSurname(null);
		SurnameMatchResult result = utils.matchSurname(student, master);
		assertTrue(result.getSurnamePoints() == 10);
	}

	@Test
	public void testMatchSex_ShouldScore0() {
		ScoringUtils utils = new ScoringUtils();
		PenMatchStudent student = createPenMatchStudent();
		PenMasterRecord master = createPenMasterRecord();
		student.setSex("M");
		master.setSex("F");

		assertTrue(utils.matchSex(student, master) == 0);
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
