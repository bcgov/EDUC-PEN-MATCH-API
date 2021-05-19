package ca.bc.gov.educ.api.penmatch.rest;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import ca.bc.gov.educ.api.penmatch.exception.PENMatchRuntimeException;
import ca.bc.gov.educ.api.penmatch.filter.FilterOperation;
import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
import ca.bc.gov.educ.api.penmatch.struct.*;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.penmatch.constants.EventType.GET_PAGINATED_STUDENT_BY_CRITERIA;
import static ca.bc.gov.educ.api.penmatch.constants.EventType.GET_STUDENT;
import static ca.bc.gov.educ.api.penmatch.filter.FilterOperation.EQUAL;
import static ca.bc.gov.educ.api.penmatch.filter.FilterOperation.STARTS_WITH;
import static ca.bc.gov.educ.api.penmatch.struct.Condition.AND;
import static ca.bc.gov.educ.api.penmatch.struct.Condition.OR;
import static ca.bc.gov.educ.api.penmatch.struct.ValueType.DATE;
import static ca.bc.gov.educ.api.penmatch.struct.ValueType.STRING;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
@Component
@Slf4j
public class RestUtils {
  /**
   * The constant LEGAL_LAST_NAME.
   */
  public static final String LEGAL_LAST_NAME = "legalLastName";
  /**
   * The constant PAGINATED.
   */
  public static final String PAGINATED = "/paginated";
  /**
   * The constant SEARCH_CRITERIA_LIST.
   */
  public static final String SEARCH_CRITERIA_LIST = "searchCriteriaList";
  /**
   * The constant PAGE_SIZE.
   */
  public static final String PAGE_SIZE = "pageSize";
  /**
   * The constant LEGAL_FIRST_NAME.
   */
  public static final String LEGAL_FIRST_NAME = "legalFirstName";
  /**
   * The constant MINCODE.
   */
  public static final String MINCODE = "mincode";
  /**
   * The constant LOCAL_ID.
   */
  public static final String LOCAL_ID = "localID";
  /**
   * The constant DOB.
   */
  public static final String DOB = "dob";

  /**
   * The constant status.
   */
  public static final String STATUS_CODE = "statusCode";

  /**
   * The constant deceased.
   */
  public static final String DECEASED = "D";

  /**
   * The constant merged.
   */
  public static final String MERGED = "M";

  /**
   * The constant DOB_FORMATTER_SHORT.
   */
  private static final DateTimeFormatter DOB_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyyMMdd");
  /**
   * The constant DOB_FORMATTER_LONG.
   */
  private static final DateTimeFormatter DOB_FORMATTER_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  public static final String STUDENT_API_TOPIC = "STUDENT_API_TOPIC";
  /**
   * The Object mapper.
   */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * The Connection.
   */
  private final Connection connection;


  /**
   * Instantiates a new Rest utils.
   *
   * @param connection the connection
   */
  public RestUtils(final NatsConnection connection) {
    this.connection = connection.getNatsCon();
  }


  /**
   * Gets pen master record by pen.
   *
   * @param pen           the pen
   * @param correlationID the correlation id
   * @return the pen master record by pen
   */
  public Optional<PenMasterRecord> getPenMasterRecordByPen(final String pen, final UUID correlationID) {
    try {
      final Event event = Event.builder().sagaId(correlationID).eventType(GET_STUDENT).eventPayload(pen).build();
      final var responseMessage = this.connection.request(STUDENT_API_TOPIC, this.objectMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
      if (responseMessage != null && responseMessage.getData() != null && responseMessage.getData().length > 0) {
        val student = this.objectMapper.readValue(responseMessage.getData(), StudentEntity.class);
        if (student == null || student.getPen() == null) {
          return Optional.empty();
        }
        return Optional.of(PenMatchUtils.convertStudentEntityToPenMasterRecord(student));
      }
    } catch (final Exception ex) {
      log.error("exception", ex);
    }
    return Optional.empty();
  }

  /**
   * Lookup with all parts list.
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param givenName     the given name
   * @param mincode       the mincode
   * @param localID       the local id
   * @param correlationID the correlation id
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<StudentEntity> lookupWithAllParts(final String dob, final String surname, final String givenName, final String mincode, final String localID, final UUID correlationID) throws JsonProcessingException {
    final LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    final SearchCriteria criteriaDob = this.getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    final List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    final List<SearchCriteria> criteriaListSurnameGiven = new LinkedList<>();
    criteriaListSurnameGiven.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    criteriaListSurnameGiven.add(this.getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));

    final List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    criteriaListMincodeLocalID.add(this.getCriteria(MINCODE, EQUAL, mincode, STRING));
    criteriaListMincodeLocalID.add(this.getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurnameGiven.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }
    if (!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

    final List<SearchCriteria> criteriaMergedDeceased = new LinkedList<>();

    final SearchCriteria criteriaMandD =
      SearchCriteria.builder().key(STATUS_CODE).operation(FilterOperation.NOT_IN).value("M,D").valueType(ValueType.STRING).build();

    criteriaMergedDeceased.add(criteriaMandD);
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaMergedDeceased).build());

    final String criteriaJSON = this.objectMapper.writeValueAsString(searches);

    return this.getStudentsByCriteria(criteriaJSON, correlationID);
  }

  /**
   * Lookup no init list.
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param mincode       the mincode
   * @param localID       the local id
   * @param correlationID the correlation id
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<StudentEntity> lookupNoInit(final String dob, final String surname, final String mincode, final String localID, final UUID correlationID) throws JsonProcessingException {

    final LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    final SearchCriteria criteriaDob = this.getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    final List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    final List<SearchCriteria> criteriaListSurname = new LinkedList<>();
    criteriaListSurname.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));

    final List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    criteriaListMincodeLocalID.add(this.getCriteria(MINCODE, EQUAL, mincode, STRING));
    criteriaListMincodeLocalID.add(this.getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurname.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }
    if (!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

    final List<SearchCriteria> criteriaMergedDeceased = new LinkedList<>();

    final SearchCriteria criteriaMandD =
      SearchCriteria.builder().key(STATUS_CODE).operation(FilterOperation.NOT_IN).value("M,D").valueType(ValueType.STRING).build();

    criteriaMergedDeceased.add(criteriaMandD);
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaMergedDeceased).build());

    final String criteriaJSON = this.objectMapper.writeValueAsString(searches);

    return this.getStudentsByCriteria(criteriaJSON, correlationID);
  }

  /**
   * Lookup no local id list.
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param givenName     the given name
   * @param correlationID the correlation id
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<StudentEntity> lookupNoLocalID(final String dob, final String surname, final String givenName, final UUID correlationID) throws JsonProcessingException {
    final LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    final SearchCriteria criteriaDob = this.getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    final List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    final List<SearchCriteria> criteriaListSurnameGiven = new LinkedList<>();
    criteriaListSurnameGiven.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    criteriaListSurnameGiven.add(this.getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));

    final List<SearchCriteria> criteriaMergedDeceased = new LinkedList<>();

    final SearchCriteria criteriaMandD =
      SearchCriteria.builder().key(STATUS_CODE).operation(FilterOperation.NOT_IN).value("M,D").valueType(ValueType.STRING).build();

    criteriaMergedDeceased.add(criteriaMandD);

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurnameGiven.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaMergedDeceased).build());

    final String criteriaJSON = this.objectMapper.writeValueAsString(searches);

    return this.getStudentsByCriteria(criteriaJSON, correlationID);
  }


  /**
   * Gets criteria.
   *
   * @param key       the key
   * @param operation the operation
   * @param value     the value
   * @param valueType the value type
   * @return the criteria
   */
  private SearchCriteria getCriteria(final String key, final FilterOperation operation, final String value, final ValueType valueType) {
    return SearchCriteria.builder().key(key).operation(operation).value(value).valueType(valueType).build();
  }

  /**
   * Gets criteria with condition.
   *
   * @param key       the key
   * @param operation the operation
   * @param value     the value
   * @param valueType the value type
   * @param condition the condition
   * @return the criteria with condition
   */
  private SearchCriteria getCriteriaWithCondition(final String key, final FilterOperation operation, final String value, final ValueType valueType, final Condition condition) {
    return SearchCriteria.builder().key(key).operation(operation).value(value).valueType(valueType).condition(condition).build();
  }

  /**
   * Lookup with no initial or local ID
   *
   * @param dob           the dob
   * @param surname       the surname
   * @param correlationID the correlation id
   * @return the list
   * @throws JsonProcessingException the json processing exception
   */
  public List<StudentEntity> lookupNoInitNoLocalID(final String dob, final String surname, final UUID correlationID) throws JsonProcessingException {
    final LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    final SearchCriteria criteriaDob = this.getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    final List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    final List<SearchCriteria> criteriaListSurname = new LinkedList<>();
    criteriaListSurname.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurname.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }

    final List<SearchCriteria> criteriaMergedDeceased = new LinkedList<>();

    final SearchCriteria criteriaMandD =
            SearchCriteria.builder().key(STATUS_CODE).operation(FilterOperation.NOT_IN).value("M,D").valueType(ValueType.STRING).build();

    criteriaMergedDeceased.add(criteriaMandD);
    searches.add(Search.builder().condition(AND).searchCriteriaList(criteriaMergedDeceased).build());

    final String criteriaJSON = this.objectMapper.writeValueAsString(searches);
    return this.getStudentsByCriteria(criteriaJSON, correlationID);
  }

  /**
   * Fetches a PEN Master Record given a student number
   *
   * @param studentID the student id
   * @param correlationID the correlation id.
   * @return the optional
   */
  @Retryable(value = {Exception.class}, backoff = @Backoff(multiplier = 2, delay = 200))
  public Optional<String> lookupStudentTruePENNumberByStudentID(final String studentID, UUID correlationID) {
    try {
      List<String> studentIDs = new ArrayList<>();
      studentIDs.add(studentID);
      final List<StudentEntity> students = getStudents(correlationID, studentIDs); // it will be always a single response since one id was passed.
      if (students.get(0) != null && students.get(0).getTrueStudentID() != null) {
        studentIDs.clear();
        studentIDs.add(students.get(0).getTrueStudentID());
        final List<StudentEntity> trueStudents = getStudents(correlationID, studentIDs); // it will be always a single response since one id was passed.
        if (trueStudents != null && trueStudents.get(0) != null) {
          return Optional.ofNullable(trueStudents.get(0).getPen());
        }
      }
    } catch (final Exception e) {
      throw new PENMatchRuntimeException("Exception while calling student api for correlation ID :: "+correlationID+" :: "+ e.getMessage());
    }
    return Optional.empty();
  }

  private List<StudentEntity> getStudents(UUID sagaId, List<String> studentIDs) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    final var event = ca.bc.gov.educ.api.penmatch.struct.Event.builder().sagaId(sagaId).eventType(EventType.GET_STUDENTS).eventPayload(JsonUtil.getJsonStringFromObject(studentIDs)).build();
    log.info("called STUDENT_API saga id :: {}, get students :: {}",sagaId, studentIDs);
    val responseEvent = JsonUtil.getJsonObjectFromByteArray(ca.bc.gov.educ.api.penmatch.struct.Event.class, this.connection.request(STUDENT_API_TOPIC, JsonUtil.getJsonBytesFromObject(event)).get(2, TimeUnit.SECONDS).getData());
    log.info("got response from STUDENT_API  :: {}", responseEvent);
    if (responseEvent.getEventOutcome() == EventOutcome.STUDENT_NOT_FOUND) {
      log.error("Students not found or student size mismatch for student IDs:: {}, this should not have happened", studentIDs);
      throw new PENMatchRuntimeException("Student not found for , " + studentIDs);
    }
    return this.objectMapper.readValue(responseEvent.getEventPayload(), new TypeReference<>() {
    });
  }

  /**
   * Get students by criteria list.
   *
   * @param criteria      the criteria
   * @param correlationID the correlation id
   * @return the list
   */
  public List<StudentEntity> getStudentsByCriteria(final String criteria, final UUID correlationID) {
    try {
      log.info("Sys Criteria: {}", criteria);
      final TypeReference<RestPageImpl<StudentEntity>> ref = new TypeReference<>() {
      };
      final var obMapper = new ObjectMapper();
      final Event event = Event.builder().sagaId(correlationID).eventType(GET_PAGINATED_STUDENT_BY_CRITERIA).eventPayload(SEARCH_CRITERIA_LIST.concat("=").concat(URLEncoder.encode(criteria, StandardCharsets.UTF_8)).concat("&").concat(PAGE_SIZE).concat("=").concat("100000")).build();
      final var responseMessage = this.connection.request(STUDENT_API_TOPIC, obMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
      if (null != responseMessage) {
        return obMapper.readValue(responseMessage.getData(), ref).getContent();
      } else {
        log.error("Either NATS timed out or the response is null");
      }

    } catch (final Exception ex) {
      log.error("exception", ex);
    }
    return new ArrayList<>();
  }


}
