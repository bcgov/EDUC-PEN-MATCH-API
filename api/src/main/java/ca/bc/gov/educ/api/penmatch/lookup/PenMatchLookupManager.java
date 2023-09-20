package ca.bc.gov.educ.api.penmatch.lookup;

import ca.bc.gov.educ.api.penmatch.exception.LookupRuntimeException;
import ca.bc.gov.educ.api.penmatch.model.v1.*;
import ca.bc.gov.educ.api.penmatch.repository.v1.ForeignSurnameRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.MatchCodesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.v1.match.SurnameFrequencyService;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
   * The constant ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON.
   */
  public static final String ERROR_OCCURRED_WHILE_WRITING_CRITERIA_AS_JSON = "Error occurred while writing criteria as JSON: ";

  /**
   * The constant ERROR_OCCURRED_DURING_LOOKUP.
   */
  public static final String ERROR_OCCURRED_DURING_LOOKUP = "Error occurred during lookup: ";
  /**
   * The Foreign surname repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final ForeignSurnameRepository foreignSurnameRepository;
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
   * The Nicknames map
   */
  private final Map<String, List<NicknamesEntity>> nicknamesMap = new ConcurrentHashMap<>();
  /**
   * The Nicknames lock.
   */
  private final ReadWriteLock nicknamesLock = new ReentrantReadWriteLock();
  private final SurnameFrequencyService surnameFrequencyService;
  /**
   * The Match codes map.
   */
  private Map<String, String> matchCodesMap;

  /**
   * Instantiates a new Pen match lookup manager.
   *
   * @param foreignSurnameRepository   the foreign surname repository
   * @param nicknamesRepository        the nicknames repository
   * @param matchCodesRepository       the match codes repository
   * @param restUtils                  the rest utils
   */
  @Autowired
  public PenMatchLookupManager(final ForeignSurnameRepository foreignSurnameRepository, final NicknamesRepository nicknamesRepository, final MatchCodesRepository matchCodesRepository, final RestUtils restUtils, final SurnameFrequencyService surnameFrequencyService) {
    this.foreignSurnameRepository = foreignSurnameRepository;
    this.nicknamesRepository = nicknamesRepository;
    this.matchCodesRepository = matchCodesRepository;
    this.restUtils = restUtils;
    this.surnameFrequencyService = surnameFrequencyService;
  }

  @Scheduled(fixedRate = 30000)
  @Caching(evict = {
          @CacheEvict(value="lookupAllParts", allEntries=true),
          @CacheEvict(value="lookupNoInit", allEntries=true) })
  public void evictAllCachesAtIntervals() {
    log.debug("Evicting cache for lookups");
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
  @Cacheable("lookupAllParts")
  public List<StudentEntity> lookupWithAllParts(String dob, String surname, String givenName, String mincode, String localID, UUID correlationID) {
    try {
      return restUtils.lookupWithAllParts(dob, surname, givenName, mincode, localID, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
      throw new LookupRuntimeException(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
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
  @Cacheable("lookupNoInit")
  public List<StudentEntity> lookupNoInit(String dob, String surname, String mincode, String localID, UUID correlationID) {
    try {
      return restUtils.lookupNoInit(dob, surname, mincode, localID, correlationID);
    } catch (JsonProcessingException e) {
      log.error(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
      throw new LookupRuntimeException(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
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
      log.error(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
      throw new LookupRuntimeException(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
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
      log.error(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
      throw new LookupRuntimeException(ERROR_OCCURRED_DURING_LOOKUP + e.getMessage());
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
   * @param correlationID the correlation or transaction id
   * @return the string
   */
  public String lookupStudentTruePENNumberByStudentID(String studentID, UUID correlationID) {
    if (studentID != null) {
      var truePenOptional = restUtils.lookupStudentTruePENNumberByStudentID(studentID, correlationID);
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
        penMatchTransactionNames.getNicknames().add(baseNickname);
      }

      List<NicknamesEntity> tempNicknamesList = getNicknames(baseNickname);
      for (NicknamesEntity nickEntity : tempNicknamesList) {
        if (!StringUtils.equals(nickEntity.getNickname2(), givenNameUpper)) {
          penMatchTransactionNames.getNicknames().add(StringUtils.trimToEmpty(nickEntity.getNickname2()));
        }
      }
    }
  }

  /**
   * Check frequency of surname
   */
  public Integer lookupSurnameFrequency(String fullStudentSurname) {
    return surnameFrequencyService.lookupSurnameFrequency(fullStudentSurname);
  }

  /**
   * Lookup foreign surname
   *
   * @param surname  the surname
   * @param ancestry the ancestry
   * @return the boolean
   */
  public boolean lookupForeignSurname(String surname, String ancestry) {
    var curDate = LocalDate.now();

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
    var writeLock = nicknamesLock.writeLock();
    try {
      writeLock.lock();
      mapNicknames(getNicknamesRepository().findAll());

      log.info("loaded {} entries into nicknames map ", nicknamesMap.values().size());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Map nickname.
   *
   * @param entities the entity list
   */
// map as (givenName, list of Nicknames entity)
  private void mapNicknames(List<NicknamesEntity> entities) {
    for (NicknamesEntity entity : entities) {
      String givenName = entity.getNickname1();
      var key = StringUtils.trimToNull(givenName);
      List<NicknamesEntity> nicknames;

      if (this.nicknamesMap.containsKey(key)) {
        nicknames = this.nicknamesMap.get(key);
      } else {
        nicknames = new ArrayList<>();
      }

      if (!nicknames.contains(entity)) {
        nicknames.add(entity);
        this.nicknamesMap.put(key, nicknames);
      }
    }

    for (NicknamesEntity entity : entities) {
      String givenName = entity.getNickname2();
      var key = StringUtils.trimToNull(givenName);
      List<NicknamesEntity> nicknames;

      if (this.nicknamesMap.containsKey(key)) {
        nicknames = this.nicknamesMap.get(key);
      } else {
        nicknames = new ArrayList<>();
      }

      if (!nicknames.contains(entity)) {
        nicknames.add(entity);
        this.nicknamesMap.put(key, nicknames);
      }
    }


  }

}
