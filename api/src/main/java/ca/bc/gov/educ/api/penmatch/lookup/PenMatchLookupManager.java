package ca.bc.gov.educ.api.penmatch.lookup;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ca.bc.gov.educ.api.penmatch.model.MatchCodesEntity;
import ca.bc.gov.educ.api.penmatch.repository.MatchCodesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
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
    private final MatchCodesRepository matchCodesRepository;

    @Autowired
    private final EntityManager entityManager;

    @Autowired
    public PenMatchLookupManager(final EntityManager entityManager, final PenDemographicsRepository penDemographicsRepository, final NicknamesRepository nicknamesRepository, final SurnameFrequencyRepository surnameFrequencyRepository, final MatchCodesRepository matchCodesRepository) {
        this.penDemographicsRepository = penDemographicsRepository;
        this.nicknamesRepository = nicknamesRepository;
        this.surnameFrequencyRepository = surnameFrequencyRepository;
        this.matchCodesRepository = matchCodesRepository;
        this.entityManager = entityManager;
    }

    /**
     * Local ID is not blank, lookup with all parts
     *
     * @return
     */
    public List<PenDemographicsEntity> lookupWithAllParts(String dob, String surname, String givenName, String mincode, String localID) {
        Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogWithAllParts");
        lookupNoInitQuery.setParameter(1, dob);
        lookupNoInitQuery.setParameter(2, surname + "%");
        lookupNoInitQuery.setParameter(3, givenName + "%");
        lookupNoInitQuery.setParameter(4, mincode);
        lookupNoInitQuery.setParameter(5, localID);

        return lookupNoInitQuery.getResultList();
    }

    /**
     * Looking using local ID but don't use initial
     */
    public List<PenDemographicsEntity> lookupNoInit(String dob, String surname, String mincode, String localID) {
        Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoInit");
        lookupNoInitQuery.setParameter(1, dob);
        lookupNoInitQuery.setParameter(2, surname + "%");
        lookupNoInitQuery.setParameter(3, mincode);
        lookupNoInitQuery.setParameter(4, localID);

        return lookupNoInitQuery.getResultList();

    }

    /**
     * Perform lookup with no local ID
     */
    public List<PenDemographicsEntity> lookupNoLocalID(String dob, String surname, String givenName) {
        Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoLocalID");
        lookupNoInitQuery.setParameter(1, dob);
        lookupNoInitQuery.setParameter(2, surname + "%");
        lookupNoInitQuery.setParameter(3, givenName + "%");

        return lookupNoInitQuery.getResultList();
    }

    /**
     * Lookup with no initial or local ID
     */
    public List<PenDemographicsEntity> lookupNoInitNoLocalID(String dob, String surname) {
        Query lookupNoInitQuery = entityManager.createNamedQuery("PenDemographicsEntity.penDemogNoInitNoLocalID");
        lookupNoInitQuery.setParameter(1, dob);
        lookupNoInitQuery.setParameter(2, surname + "%");

        return lookupNoInitQuery.getResultList();
    }

    /**
     * Fetches a PEN Master Record given a student number
     */
    public PenMasterRecord lookupStudentByPEN(String studentNumber) {
        if (studentNumber != null) {
            Optional<PenDemographicsEntity> demog = getPenDemographicsRepository().findByStudNo(studentNumber);
            if (demog.isPresent()) {
                return PenMatchUtils.convertPenDemogToPenMasterRecord(demog.get());
            }
        }
        return null;
    }

    /**
     * Look up nicknames Nickname1 (by convention) is the "base" nickname. For
     * example, we would expect the following in the nickname file:
     * <p>
     * Nickname 1 Nickname 2 JAMES JIM JAMES JIMMY JAMES JAIMIE
     */
    public void lookupNicknames(PenMatchNames penMatchTransactionNames, String givenName) {
        if (givenName == null || givenName.length() < 1) {
            return;
        }

        String givenNameUpper = givenName;

        // Part 1 - Find the base nickname
        String baseNickname = null;

        List<NicknamesEntity> nicknamesBaseList = getNicknamesRepository().findAllByNickname1OrNickname2(givenNameUpper, givenNameUpper);
        if (nicknamesBaseList != null && !nicknamesBaseList.isEmpty()) {
            baseNickname = nicknamesBaseList.get(0).getNickname1().trim();
        }

        // Part 2 - Base nickname has been found; now find all the nickname2's,
        // bypassing the one that is the same as the given name in the transaction.
        // The base nickname should be stored as well if it is not the same as the given
        // name
        if (baseNickname != null) {
            if (!baseNickname.equals(givenNameUpper)) {
                penMatchTransactionNames.setNickname1(baseNickname);
            }

            List<NicknamesEntity> tempNicknamesList = getNicknamesRepository().findAllByNickname1OrNickname2(baseNickname, baseNickname);

            for (NicknamesEntity nickEntity : tempNicknamesList) {
                if (!nickEntity.getNickname2().equals(givenNameUpper)) {
                    PenMatchUtils.setNextNickname(penMatchTransactionNames, nickEntity.getNickname2().trim());
                }

                if (penMatchTransactionNames.getNickname4() != null && !penMatchTransactionNames.getNickname4().isEmpty()) {
                    break;
                }
            }
        }

    }

    /**
     * Check frequency of surname
     */
    public Integer lookupSurnameFrequency(String fullStudentSurname) {
        if (fullStudentSurname == null) {
            return 0;
        }
        // Note this returns in two different places
        Integer surnameFrequency = 0;
        List<SurnameFrequencyEntity> surnameFreqEntityList = getSurnameFrequencyRepository().findAllBySurnameStartingWith(fullStudentSurname);

        for (SurnameFrequencyEntity surnameFreqEntity : surnameFreqEntityList) {
            surnameFrequency = surnameFrequency + Integer.valueOf(surnameFreqEntity.getSurnameFrequency());

            if (surnameFrequency >= VERY_FREQUENT) {
                break;
            }
        }

        return surnameFrequency;
    }

    /**
     * Lookup match codes
     */
    public String lookupMatchResult(String matchCode) {
        if (matchCode == null) {
            return null;
        }
        // Note this returns in two different places
        Optional<MatchCodesEntity> matchCodesEntity = getMatchCodesRepository().findByMatchCode(matchCode);

        if (matchCodesEntity.isPresent()) {
            return matchCodesEntity.get().getMatchResult();
        }

        return null;
    }

}
