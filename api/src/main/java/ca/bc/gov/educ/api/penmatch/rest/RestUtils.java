package ca.bc.gov.educ.api.penmatch.rest;

import ca.bc.gov.educ.api.penmatch.filter.FilterOperation;
import ca.bc.gov.educ.api.penmatch.messaging.NatsConnection;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.StudentMergeEntity;
import ca.bc.gov.educ.api.penmatch.properties.ApplicationProperties;
import ca.bc.gov.educ.api.penmatch.struct.*;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.util.PenMatchUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static ca.bc.gov.educ.api.penmatch.constants.EventType.GET_PAGINATED_STUDENT_BY_CRITERIA;
import static ca.bc.gov.educ.api.penmatch.constants.EventType.GET_STUDENT;
import static ca.bc.gov.educ.api.penmatch.filter.FilterOperation.EQUAL;
import static ca.bc.gov.educ.api.penmatch.filter.FilterOperation.STARTS_WITH;
import static ca.bc.gov.educ.api.penmatch.struct.Condition.AND;
import static ca.bc.gov.educ.api.penmatch.struct.Condition.OR;
import static ca.bc.gov.educ.api.penmatch.struct.ValueType.DATE;
import static ca.bc.gov.educ.api.penmatch.struct.ValueType.STRING;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

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
   * The constant DOB_FORMATTER_SHORT.
   */
  private static final DateTimeFormatter DOB_FORMATTER_SHORT = DateTimeFormatter.ofPattern("yyyyMMdd");
  /**
   * The constant DOB_FORMATTER_LONG.
   */
  private static final DateTimeFormatter DOB_FORMATTER_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  /**
   * The Object mapper.
   */
  private final ObjectMapper objectMapper = new ObjectMapper();
  /**
   * The Props.
   */
  private final ApplicationProperties props;

  /**
   * The Connection.
   */
  private final Connection connection;

  /**
   * The Web client.
   */
  private final WebClient webClient;

  /**
   * Instantiates a new Rest utils.
   *
   * @param props      the props
   * @param connection the connection
   * @param webClient  the web client
   */
  public RestUtils(final ApplicationProperties props, final NatsConnection connection, final WebClient webClient) {
    this.props = props;
    this.connection = connection.getNatsCon();
    this.webClient = webClient;
  }


  /**
   * Gets pen master record by pen.
   *
   * @param pen           the pen
   * @param correlationID the correlation id
   * @return the pen master record by pen
   */
  public Optional<PenMasterRecord> getPenMasterRecordByPen(final String pen, final UUID correlationID) {
    final var obMapper = new ObjectMapper();
    try {
      final Event event = Event.builder().sagaId(correlationID).eventType(GET_STUDENT).eventPayload(pen).build();
      final var responseMessage = this.connection.request("STUDENT_API_TOPIC", obMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
      if (responseMessage.getData() != null && responseMessage.getData().length > 0) {
        val student = obMapper.readValue(responseMessage.getData(), StudentEntity.class);
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
    if (StringUtils.isNotBlank(surname)) {
      criteriaListSurnameGiven.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }
    if (StringUtils.isNotBlank(givenName)) {
      criteriaListSurnameGiven.add(this.getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));
    }

    final List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    if (StringUtils.isNotBlank(mincode)) {
      criteriaListMincodeLocalID.add(this.getCriteria(MINCODE, EQUAL, mincode, STRING));
    }
    if (StringUtils.isNotBlank(localID)) {
      criteriaListMincodeLocalID.add(this.getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));
    }

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurnameGiven.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }
    if (!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

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
    if (StringUtils.isNotBlank(surname)) {
      criteriaListSurname.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }

    final List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    if (StringUtils.isNotBlank(mincode)) {
      criteriaListMincodeLocalID.add(this.getCriteria(MINCODE, EQUAL, mincode, STRING));
    }
    if (StringUtils.isNotBlank(localID)) {
      criteriaListMincodeLocalID.add(this.getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));
    }

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurname.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }
    if (!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

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
    if (StringUtils.isNotBlank(surname)) {
      criteriaListSurnameGiven.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }
    if (StringUtils.isNotBlank(givenName)) {
      criteriaListSurnameGiven.add(this.getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));
    }

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurnameGiven.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }

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
    if (StringUtils.isNotBlank(surname)) {
      criteriaListSurname.add(this.getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }

    final List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if (!criteriaListSurname.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }

    final String criteriaJSON = this.objectMapper.writeValueAsString(searches);
    return this.getStudentsByCriteria(criteriaJSON, correlationID);
  }

  /**
   * Fetches a PEN Master Record given a student number
   *
   * @param studentID the student id
   * @return the optional
   */
  public Optional<String> lookupStudentTruePENNumberByStudentID(final String studentID) {
    final List<StudentMergeEntity> studentResponse = this.webClient.get().uri(this.props.getStudentApiURL(), uri -> uri.path("/".concat(studentID).concat("/merges?mergeDirection=TO")).build()).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).retrieve().bodyToFlux(StudentMergeEntity.class).collectList().block();

    if (studentResponse != null && !studentResponse.isEmpty()) {
      return Optional.ofNullable(StringUtils.trim(Objects.requireNonNull(studentResponse).get(0).getMergeStudent().getPen()));
    }
    return Optional.empty();
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
      final TypeReference<RestPageImpl<StudentEntity>> ref = new TypeReference<>() {
      };
      final var obMapper = new ObjectMapper();
      final Event event = Event.builder().sagaId(correlationID).eventType(GET_PAGINATED_STUDENT_BY_CRITERIA).eventPayload(SEARCH_CRITERIA_LIST.concat("=").concat(URLEncoder.encode(criteria, StandardCharsets.UTF_8)).concat("&").concat(PAGE_SIZE).concat("=").concat("100000")).build();
      final var responseMessage = this.connection.request("STUDENT_API_TOPIC", obMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
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
