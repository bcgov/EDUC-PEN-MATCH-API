package ca.bc.gov.educ.api.penmatch.lookup;

import ca.bc.gov.educ.api.penmatch.model.v1.*;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.repository.v1.ForeignSurnameRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchCodesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * The type Pen match lookup manager.
 */
@Service
@Slf4j
public class PenMatchLookupManager {

  /**
   * The constant VERY_FREQUENT.
   */
  public static final Integer VERY_FREQUENT = 500;
  /**
   * The constant ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON.
   */
  public static final String ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON = "Error occurred while writing criteria as JSON: ";
  /**
   * The constant PARAMETERS_ATTRIBUTE.
   */
  private static final String PARAMETERS_ATTRIBUTE = "parameters";
  /**
   * The Foreign surname repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final ForeignSurnameRepository foreignSurnameRepository;
  /**
   * The Surname frequency repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SurnameFrequencyRepository surnameFrequencyRepository;
  /**
   * The Nicknames repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final NicknamesRepository nicknamesRepository;
  /**
   * The Match codes repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final MatchCodesRepository matchCodesRepository;
  /**
   * The Rest utils.
   */
  private final RestUtils restUtils;
  /**
   * The Props.
   */
  private final ApplicationProperties props;
  /**
   * The Match codes map.
   */
  private Map<String, String> matchCodesMap;
  /**
   * The Nicknames map
   */
  private final Map<String, List<NicknamesEntity>> nicknamesMap = new ConcurrentHashMap<>();

  /**
   * The Nicknames lock.
   */
  private final ReadWriteLock nicknamesLock = new ReentrantReadWriteLock();

  /**
   * Instantiates a new Pen match lookup manager.
   *
   * @param foreignSurnameRepository   the foreign surname repository
   * @param nicknamesRepository        the nicknames repository
   * @param surnameFrequencyRepository the surname frequency repository
   * @param matchCodesRepository       the match codes repository
   * @param restUtils                  the rest utils
   * @param props                      the props
   */
  @Autowired
  public PenMatchLookupManager(final ForeignSurnameRepository foreignSurnameRepository, final NicknamesRepository nicknamesRepository, final SurnameFrequencyRepository surnameFrequencyRepository, final MatchCodesRepository matchCodesRepository, final RestUtils restUtils, final ApplicationProperties props) {
    this.foreignSurnameRepository = foreignSurnameRepository;
    this.nicknamesRepository = nicknamesRepository;
    this.surnameFrequencyRepository = surnameFrequencyRepository;
    this.matchCodesRepository = matchCodesRepository;
    this.restUtils = restUtils;
    this.props = props;
  }

  /**
   * Local ID is not blank, lookup with all parts
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param givenName     the given name
   * @param mincode       the mincode
   * @param localID       the local id
   * @param correlationID the correlation id
   * @return the list
   */
  public List<StudentEntity> lookupWithAllParts(String dob, String surname, String givenName, String mincode, String localID, UUID correlationID) {
    try {
      return restUtils.lookupWithAllParts(dob, surname, givenName, mincode, localID, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON + e.getMessage());
      return new ArrayList<>();
    }
  }


  /**
   * Looking using local ID but don't use initial
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param mincode       the mincode
   * @param localID       the local id
   * @param correlationID the correlation id
   * @return the list
   */
  public List<StudentEntity> lookupNoInit(String dob, String surname, String mincode, String localID, UUID correlationID) {
    try {
      return restUtils.lookupNoInit(dob, surname, mincode, localID, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Perform lookup with no local ID
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param givenName     the given name
   * @param correlationID the correlation id
   * @return the list
   */
  public List<StudentEntity> lookupNoLocalID(String dob, String surname, String givenName, UUID correlationID) {
    try {
      return restUtils.lookupNoLocalID(dob, surname, givenName, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Lookup with no initial or local ID
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param correlationID the correlation id
   * @return the list
   */
  public List<StudentEntity> lookupNoInitNoLocalID(String dob, String surname, UUID correlationID) {
    try {
      return restUtils.lookupNoInitNoLocalID(dob, surname, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON + e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * Fetches a PEN Master Record given a student number
   *
   * @param pen           the pen
   * @param correlationID the correlation id
   * @return the optional
   */
  public Optional<PenMasterRecord> lookupStudentByPEN(String pen, UUID correlationID) {
    if (StringUtils.isNotBlank(pen)) {
      return restUtils.getPenMasterRecordByPen(pen, correlationID);
    }
    return Optional.empty();
  }


  /**
   * Fetches a PEN Master Record given a student number
   *
   * @param studentID the student id
   * @return the string
   */
  public String lookupStudentTruePENNumberByStudentID(String studentID) {
    if (studentID != null) {
      var truePenOptional = restUtils.lookupStudentTruePENNumberByStudentID(studentID);
      if (truePenOptional.isPresent()) {
        return truePenOptional.get();
      }
    }
    return null;
  }


  /**
   * Look up nicknames Nickname1 (by convention) is the "base" nickname. For
   * example, we would expect the following in the nickname file:
   * <p>
   * Nickname 1 Nickname 2 JAMES JIM JAMES JIMMY JAMES JAIMIE
   *
   * @param givenName the given name
   * @return the list
   */
  public List<NicknamesEntity> lookupNicknamesOnly(String givenName) {
    if (givenName == null || givenName.length() < 1) {
      return new ArrayList<>();
    }

    String givenNameUpper = givenName.toUpperCase();
    return getNicknames(givenNameUpper);
  }

  /**
   * Look up nicknames Nickname1 (by convention) is the "base" nickname. For
   * example, we would expect the following in the nickname file:
   * <p>
   * Nickname 1 Nickname 2 JAMES JIM JAMES JIMMY JAMES JAIMIE
   *
   * @param penMatchTransactionNames the pen match transaction names
   * @param givenName                the given name
   */
  public void lookupNicknames(PenMatchNames penMatchTransactionNames, String givenName) {
    if (StringUtils.isBlank(givenName)) {
      return;
    }

    String givenNameUpper = givenName.toUpperCase();

    // Part 1 - Find the base nickname
    String baseNickname = null;

    List<NicknamesEntity> nicknamesBaseList = getNicknames(givenNameUpper);
    if (!nicknamesBaseList.isEmpty()) {
      baseNickname = StringUtils.trimToNull(nicknamesBaseList.get(0).getNickname1());
    }

    // Part 2 - Base nickname has been found; now find all the nickname2's,
    // bypassing the one that is the same as the given name in the transaction.
    // The base nickname should be stored as well if it is not the same as the given
    // name
    if (baseNickname != null) {

      if (!StringUtils.equals(baseNickname, givenNameUpper)) {
        penMatchTransactionNames.setNickname1(baseNickname);
      }

      List<NicknamesEntity> tempNicknamesList = getNicknames(baseNickname);
      for (NicknamesEntity nickEntity : tempNicknamesList) {
        if (!StringUtils.equals(nickEntity.getNickname2(), givenNameUpper)) {
          PenMatchUtils.setNextNickname(penMatchTransactionNames, StringUtils.trimToEmpty(nickEntity.getNickname2()));
        }
        if (StringUtils.isNotBlank(penMatchTransactionNames.getNickname4())) {
          break;
        }
      }
    }
  }


  /**
   * Check frequency of surname
   *
   * @param fullStudentSurname the full student surname
   * @return the integer
   */
  public Integer lookupSurnameFrequency(String fullStudentSurname) {
    if (fullStudentSurname == null) {
      return 0;
    }
    // Note this returns in two different places
    int surnameFrequency = 0;
    var surnameFreqEntityList = getSurnameFrequencyRepository().findAllBySurnameStartingWith(fullStudentSurname);
    for (SurnameFrequencyEntity surnameFreqEntity : surnameFreqEntityList) {
      surnameFrequency += Integer.parseInt(surnameFreqEntity.getSurnameFrequency());
      if (surnameFrequency >= VERY_FREQUENT) {
        break;
      }
    }
    return surnameFrequency;
  }

  /**
   * Lookup foreign surname
   *
   * @param surname  the surname
   * @param ancestry the ancestry
   * @return the boolean
   */
  public boolean lookupForeignSurname(String surname, String ancestry) {
    LocalDate curDate = LocalDate.now();

    Optional<ForeignSurnamesEntity> foreignSurnamesEntities = getForeignSurnameRepository().findBySurnameAndAncestryAndEffectiveDateLessThanEqualAndExpiryDateGreaterThanEqual(surname, ancestry, curDate, curDate);

    return foreignSurnamesEntities.isPresent();
  }

  /**
   * Lookup match codes
   *
   * @param matchCode the match code
   * @return the string
   */
  public String lookupMatchResult(String matchCode) {
    if (matchCode == null) {
      return null;
    }

    if (matchCodesMap == null) {
      matchCodesMap = new ConcurrentHashMap<>();
      List<MatchCodesEntity> matchCodesEntities = getMatchCodesRepository().findAll();
      for (MatchCodesEntity entity : matchCodesEntities) {
        matchCodesMap.put(entity.getMatchCode(), entity.getMatchResult());
      }
    }

    if (matchCodesMap.containsKey(matchCode)) {
      return matchCodesMap.get(matchCode);
    }

    return matchCode;
  }

  /**
   * Reload cache.
   * - Evict cache every 24 hours and reload again
   */
  @Scheduled(fixedRate = 86400000)
  public void reloadCache() {
    log.info("Evicting match codes cache");
    if (matchCodesMap != null) {
      matchCodesMap.clear();
    }
    matchCodesMap = getMatchCodesRepository().findAll().stream().collect(Collectors.toConcurrentMap(MatchCodesEntity::getMatchCode, MatchCodesEntity::getMatchResult));
    log.info("Reloaded match codes into cache. {} entries", matchCodesMap.size());

    log.info("Reloading nicknames cache");
    nicknamesMap.clear();
    this.setNicknames();
    log.info("Reloaded nicknames into cache");
  }

  /**
   * Init.
   */
  @PostConstruct
  public void init() {
    log.info("Loading Match codes during startup.");
    matchCodesMap = getMatchCodesRepository().findAll().stream().collect(Collectors.toConcurrentMap(MatchCodesEntity::getMatchCode, MatchCodesEntity::getMatchResult));
    log.info("Loaded Match codes during startup. {} entries", matchCodesMap.size());

    log.info("Loading Nicknames during startup.");
    this.setNicknames();
    log.info("Loaded Nicknames during startup.");
  }

  /**
   * Gets nicknames.
   *
   * @param givenName the given name
   * @return the nicknames
   */
  public List<NicknamesEntity> getNicknames(String givenName) {
    String givenNameUpper = givenName.toUpperCase();
    if (this.nicknamesMap.containsKey(givenNameUpper)) {
      return this.nicknamesMap.get(givenNameUpper);
    }

    return new ArrayList<>();
  }

  /**
   * Sets nicknames.
   */
  private void setNicknames() {
    Lock writeLock = nicknamesLock.writeLock();
    try {
      writeLock.lock();
      getNicknamesRepository().findAll().forEach(e -> mapNickname(e.getNickname1(), e));
      log.info("loaded {} entries into nicknames map ", nicknamesMap.values().size());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Map nickname.
   *
   * @param givenName the given name
   * @param nickName  the nick name
   */
// map as (givenName, list of Nicknames entity)
  private void mapNickname(String givenName, NicknamesEntity nickName) {
    List<NicknamesEntity> nicknames;
    String key = StringUtils.trimToNull(givenName);
    if (this.nicknamesMap.containsKey(key)) {
      nicknames = this.nicknamesMap.get(key);
    } else {
      nicknames = new ArrayList<>();
    }

    if (!nicknames.contains(nickName)) {
      nicknames.add(nickName);
      this.nicknamesMap.put(key, nicknames);
    }
  }

}
