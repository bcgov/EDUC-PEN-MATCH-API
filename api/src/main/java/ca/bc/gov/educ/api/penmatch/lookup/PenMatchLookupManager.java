package ca.bc.gov.educ.api.penmatch.lookup;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.exception.PENMatchRuntimeException;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import lombok.AccessLevel;
import lombok.Getter;

@Service
@SuppressWarnings("unchecked")
public class PenMatchLookupManager {

	public static final String CHECK_DIGIT_ERROR_CODE_000 = "000";
	public static final String CHECK_DIGIT_ERROR_CODE_001 = "001";
	public static final Integer VERY_FREQUENT = 500;
	public static final Integer NOT_VERY_FREQUENT = 50;
	public static final Integer VERY_RARE = 5;

	@Getter(AccessLevel.PRIVATE)
	private final SurnameFrequencyRepository surnameFrequencyRepository;

	@Getter(AccessLevel.PRIVATE)
	private final PenDemographicsRepository penDemographicsRepository;

	@Getter(AccessLevel.PRIVATE)
	private final NicknamesRepository nicknamesRepository;

	@Getter(AccessLevel.PRIVATE)
	private final EntityManager entityManager;

	@Autowired
	public PenMatchLookupManager(final EntityManager entityManager,
			final PenDemographicsRepository penDemographicsRepository, final NicknamesRepository nicknamesRepository,
			final SurnameFrequencyRepository surnameFrequencyRepository) {
		this.penDemographicsRepository = penDemographicsRepository;
		this.nicknamesRepository = nicknamesRepository;
		this.surnameFrequencyRepository = surnameFrequencyRepository;
		this.entityManager = entityManager;
	}

	/**
	 * Local ID is not blank, lookup with all parts
	 * 
	 * @return
	 */
	public List<PenDemographicsEntity> lookupWithAllParts(PenMatchStudent student, PenMatchSession session) {
		Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogWithAllParts");
		lookupNoInitQuery.setParameter(1, student.getDob());
		lookupNoInitQuery.setParameter(2, student.getSurname() + "%");
		lookupNoInitQuery.setParameter(3, student.getGivenName() + "%");
		lookupNoInitQuery.setParameter(4, student.getMincode());
		lookupNoInitQuery.setParameter(5, student.getLocalID());

		return lookupNoInitQuery.getResultList();
	}

	/**
	 * 
	 * Looking using local ID but don't use initial
	 * 
	 * @param student
	 * @param session
	 * @return
	 */
	public List<PenDemographicsEntity> lookupNoInit(PenMatchStudent student, PenMatchSession session) {
		Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoInit");
		lookupNoInitQuery.setParameter(1, student.getDob());
		lookupNoInitQuery.setParameter(2, student.getSurname() + "%");
		lookupNoInitQuery.setParameter(3, student.getMincode());
		lookupNoInitQuery.setParameter(4, student.getLocalID());

		return lookupNoInitQuery.getResultList();

	}

	/**
	 * Perform lookup with no local ID
	 * 
	 * @return
	 */
	public List<PenDemographicsEntity> lookupNoLocalID(PenMatchStudent student, PenMatchSession session) {
		Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoLocalID");
		lookupNoInitQuery.setParameter(1, student.getDob());
		lookupNoInitQuery.setParameter(2, student.getSurname() + "%");
		lookupNoInitQuery.setParameter(3, student.getGivenName() + "%");

		return lookupNoInitQuery.getResultList();
	}

	/**
	 * Lookup with no initial or local ID
	 * 
	 * @param student
	 * @param session
	 */
	public List<PenDemographicsEntity> lookupNoInitNoLocalID(PenMatchStudent student, PenMatchSession session) {
		Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoInitNoLocalID");
		lookupNoInitQuery.setParameter(1, student.getDob());
		lookupNoInitQuery.setParameter(2, student.getSurname() + "%");

		return lookupNoInitQuery.getResultList();
	}

	/**
	 * Fetches a PEN Master Record given a student number
	 * 
	 * @param studentNumber
	 * @return
	 */
	public PenMasterRecord lookupStudentByPEN(String studentNumber) {
		Optional<PenDemographicsEntity> demog = getPenDemographicsRepository().findByStudNo(studentNumber);
		if (demog.isPresent()) {
			PenDemographicsEntity entity = demog.get();
			return PenMatchUtils.convertPenDemogToPenMasterRecord(entity);
		}

		throw new PENMatchRuntimeException("No PEN Demog master record found for student number: " + studentNumber);
	}

	/**
	 * Look up nicknames Nickname1 (by convention) is the "base" nickname. For
	 * example, we would expect the following in the nickname file:
	 *
	 * Nickname 1 Nickname 2 JAMES JIM JAMES JIMMY JAMES JAIMIE
	 */
	public void lookupNicknames(PenMatchNames penMatchTransactionNames, String givenName) {
		if (givenName == null || givenName.length() < 1) {
			return;
		}

		// Part 1 - Find the base nickname
		String baseNickname = null;

		List<NicknamesEntity> nicknamesBaseList = getNicknamesRepository().findByNickname1OrNickname2(givenName,
				givenName);
		if (nicknamesBaseList != null && !nicknamesBaseList.isEmpty()) {
			baseNickname = nicknamesBaseList.get(0).getNickname1();
		}

		// Part 2 - Base nickname has been found; now find all the nickname2's,
		// bypassing the one that is the same as the given name in the transaction.
		// The base nickname should be stored as well if it is not the same as the given
		// name
		if (baseNickname != null) {
			if (!baseNickname.equals(givenName)) {
				penMatchTransactionNames.setNickname1(baseNickname);
			}

			List<NicknamesEntity> tempNicknamesList;

			String currentNickname1 = nicknamesBaseList.get(0).getNickname1();
			String currentNickname2 = nicknamesBaseList.get(0).getNickname2();

			for (int i = 0; i < 3; i++) {
				tempNicknamesList = getNicknamesRepository().findByNickname1OrNickname2(currentNickname1,
						currentNickname2);
				if (!PenMatchUtils.hasGivenNameAsNickname2(tempNicknamesList, givenName)) {
					PenMatchUtils.setNextNickname(penMatchTransactionNames, tempNicknamesList.get(0).getNickname2());
				}
				currentNickname1 = tempNicknamesList.get(0).getNickname1();
				currentNickname2 = tempNicknamesList.get(0).getNickname2();
			}
		}

	}

	/**
	 * Check frequency of surname
	 * 
	 * @return
	 */
	public Integer lookupSurnameFrequency(String fullStudentSurname) {
		// TODO Implement this
		// Note this returns in two different places
		Integer surnameFrequency = 0;
		String nameForSearch = fullStudentSurname;

		while (surnameFrequency < VERY_FREQUENT) {
			Optional<SurnameFrequencyEntity> surnameEntity = getSurnameFrequencyRepository()
					.findBySurname(nameForSearch);
			if (surnameEntity.isPresent()) {
				surnameFrequency = surnameFrequency + Integer.valueOf(surnameEntity.get().getSurnameFrequency());
				nameForSearch = surnameEntity.get().getSurname();
			} else {
				break;
			}
		}

		return surnameFrequency;
	}

}
