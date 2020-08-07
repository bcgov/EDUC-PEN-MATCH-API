package ca.bc.gov.educ.api.penmatch.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ca.bc.gov.educ.api.penmatch.enumeration.PenStatus;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;

public class PenMatchUtils {

	/**
	 * Utility method which sets the penMatchTransactionNames
	 * 
	 * @param penMatchTransactionNames
	 * @param nextNickname
	 */
	public void setNextNickname(PenMatchNames penMatchTransactionNames, String nextNickname) {
		if (penMatchTransactionNames.getNickname1() == null || penMatchTransactionNames.getNickname1().length() < 1) {
			penMatchTransactionNames.setNickname1(nextNickname);
		} else if (penMatchTransactionNames.getNickname2() == null
				|| penMatchTransactionNames.getNickname2().length() < 1) {
			penMatchTransactionNames.setNickname2(nextNickname);
		} else if (penMatchTransactionNames.getNickname3() == null
				|| penMatchTransactionNames.getNickname3().length() < 1) {
			penMatchTransactionNames.setNickname3(nextNickname);
		} else if (penMatchTransactionNames.getNickname4() == null
				|| penMatchTransactionNames.getNickname4().length() < 1) {
			penMatchTransactionNames.setNickname4(nextNickname);
		}
	}

	/**
	 * Utility method to determine if the nickname list contains
	 * 
	 * @param nicknamesList
	 * @param givenName
	 * @return
	 */
	public boolean hasGivenNameAsNickname2(List<NicknamesEntity> nicknamesList, String givenName) {
		if (nicknamesList != null && !nicknamesList.isEmpty() && nicknamesList.get(0).getNickname2().equals(givenName)) {
			return true;
		}
		return false;
	}

	/**
	 * Converts PEN Demog record to a PEN Master record
	 * 
	 * @param entity
	 * @return
	 */
	public PenMasterRecord convertPenDemogToPenMasterRecord(PenDemographicsEntity entity) {
		PenMasterRecord masterRecord = new PenMasterRecord();

		masterRecord.setStudentNumber(entity.getStudNo());
		masterRecord.setDob(entity.getStudBirth());
		masterRecord.setSurname(entity.getStudSurname());
		masterRecord.setGiven(entity.getStudGiven());
		masterRecord.setMiddle(entity.getStudMiddle());
		masterRecord.setUsualSurname(entity.getUsualSurname());
		masterRecord.setUsualGivenName(entity.getUsualGiven());
		masterRecord.setUsualMiddleName(entity.getUsualMiddle());
		masterRecord.setPostal(entity.getPostalCode());
		masterRecord.setSex(entity.getStudSex());
		masterRecord.setGrade(entity.getGrade());
		masterRecord.setStatus(entity.getStudStatus());
		masterRecord.setMincode(entity.getMincode());
		masterRecord.setLocalId(entity.getLocalID());

		return masterRecord;
	}

	/**
	 * Check that the core data is there for a pen master add
	 * 
	 * @param student
	 */
	public void checkForCoreData(PenMatchStudent student) {
		if (student.getSurname() == null || student.getGivenName() == null || student.getDob() == null
				|| student.getSex() == null || student.getMincode() == null) {
			student.setPenStatus(PenStatus.G0.getValue());
		}
	}

	/**
	 * Strip off leading zeros , leading blanks and trailing blanks from the
	 * PEN_MASTER stud_local_id. Put result in MAST_PEN_ALT_LOCAL_ID
	 */
	public void normalizeLocalIDsFromMaster(PenMasterRecord master) {
		master.setAlternateLocalId("MMM");
		if (master.getLocalId() != null) {
			master.setAlternateLocalId(StringUtils.stripStart(master.getLocalId(), "0").replaceAll(" ", ""));
		}
	}

	/**
	 * This function stores all names in an object It includes some split logic for
	 * given/middle names
	 * 
	 * @param master
	 */
	public PenMatchNames storeNamesFromMaster(PenMasterRecord master) {
		String given = master.getGiven();
		String usualGiven = master.getUsualGivenName();

		PenMatchNames penMatchMasterNames;
		penMatchMasterNames = new PenMatchNames();
		penMatchMasterNames.setLegalGiven(given);
		penMatchMasterNames.setLegalMiddle(master.getMiddle());
		penMatchMasterNames.setUsualGiven(usualGiven);
		penMatchMasterNames.setUsualMiddle(master.getUsualMiddleName());

		if (given != null) {
			int spaceIndex = StringUtils.indexOf(given, " ");
			if (spaceIndex != -1) {
				penMatchMasterNames.setAlternateLegalGiven(given.substring(0, spaceIndex));
				penMatchMasterNames.setAlternateLegalMiddle(given.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(given, "-");
			if (dashIndex != -1) {
				penMatchMasterNames.setAlternateLegalGiven(given.substring(0, dashIndex));
				penMatchMasterNames.setAlternateLegalMiddle(given.substring(dashIndex));
			}
		}

		if (usualGiven != null) {
			int spaceIndex = StringUtils.indexOf(usualGiven, " ");
			if (spaceIndex != -1) {
				penMatchMasterNames.setAlternateUsualGiven(usualGiven.substring(0, spaceIndex));
				penMatchMasterNames.setAlternateUsualMiddle(usualGiven.substring(spaceIndex));
			}
			int dashIndex = StringUtils.indexOf(usualGiven, "-");
			if (dashIndex != -1) {
				penMatchMasterNames.setAlternateUsualGiven(usualGiven.substring(0, dashIndex));
				penMatchMasterNames.setAlternateUsualMiddle(usualGiven.substring(dashIndex));
			}
		}
		return penMatchMasterNames;
	}

}
