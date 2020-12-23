package ca.bc.gov.educ.api.penmatch.rest;

import ca.bc.gov.educ.api.penmatch.filter.FilterOperation;
import ca.bc.gov.educ.api.penmatch.model.StudentEntity;
import ca.bc.gov.educ.api.penmatch.model.StudentMergeEntity;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
   * The constant PARAMETERS_ATTRIBUTE.
   */
  private static final String PARAMETERS_ATTRIBUTE = "parameters";
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
   * Instantiates a new Rest utils.
   *
   * @param props      the props
   * @param connection the connection
   */
  public RestUtils(final ApplicationProperties props, Connection connection) {
    this.props = props;
    this.connection = connection;
  }

  /**
   * Gets rest template.
   *
   * @return the rest template
   */
  public RestTemplate getRestTemplate() {
    return getRestTemplate(null);
  }

  /**
   * Gets rest template.
   *
   * @param scopes the scopes
   * @return the rest template
   */
  public RestTemplate getRestTemplate(List<String> scopes) {
    log.debug("Calling get token method");
    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setClientId(props.getClientID());
    resourceDetails.setClientSecret(props.getClientSecret());
    resourceDetails.setAccessTokenUri(props.getTokenURL());
    if (scopes != null) {
      resourceDetails.setScope(scopes);
    }
    return new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext());
  }

  /**
   * Gets pen master record by pen.
   *
   * @param pen           the pen
   * @param correlationID the correlation id
   * @return the pen master record by pen
   */
  public Optional<PenMasterRecord> getPenMasterRecordByPen(String pen, UUID correlationID) {
    var obMapper = new ObjectMapper();
    try {
      Event event = Event.builder().sagaId(correlationID).eventType(GET_STUDENT).eventPayload(pen).build();
      var responseMessage = connection.request("STUDENT_API_TOPIC", obMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
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
  public List<StudentEntity> lookupWithAllParts(String dob, String surname, String givenName, String mincode, String localID, UUID correlationID) throws JsonProcessingException {
    LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    SearchCriteria criteriaDob = getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    List<SearchCriteria> criteriaListSurnameGiven = new LinkedList<>();
    if(StringUtils.isNotBlank(surname)) {
      criteriaListSurnameGiven.add(getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }
    if(StringUtils.isNotBlank(givenName)) {
      criteriaListSurnameGiven.add(getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));
    }

    List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    if(StringUtils.isNotBlank(mincode)){
      criteriaListMincodeLocalID.add(getCriteria(MINCODE, EQUAL, mincode, STRING));
    }
    if(StringUtils.isNotBlank(localID)){
      criteriaListMincodeLocalID.add(getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));
    }

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if(!criteriaListSurnameGiven.isEmpty()){
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }
    if(!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

    String criteriaJSON = objectMapper.writeValueAsString(searches);

    return getStudentsByCriteria(criteriaJSON, correlationID);
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
  public List<StudentEntity> lookupNoInit(String dob, String surname, String mincode, String localID, UUID correlationID) throws JsonProcessingException {

    LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    SearchCriteria criteriaDob = getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    List<SearchCriteria> criteriaListSurname = new LinkedList<>();
    if(StringUtils.isNotBlank(surname)) {
      criteriaListSurname.add(getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }

    List<SearchCriteria> criteriaListMincodeLocalID = new LinkedList<>();
    if(StringUtils.isNotBlank(mincode)){
      criteriaListMincodeLocalID.add(getCriteria(MINCODE, EQUAL, mincode, STRING));
    }
    if(StringUtils.isNotBlank(localID)){
      criteriaListMincodeLocalID.add(getCriteriaWithCondition(LOCAL_ID, EQUAL, localID, STRING, AND));
    }

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if(!criteriaListSurname.isEmpty()){
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }
    if(!criteriaListMincodeLocalID.isEmpty()) {
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListMincodeLocalID).build());
    }

    String criteriaJSON = objectMapper.writeValueAsString(searches);

    return getStudentsByCriteria(criteriaJSON, correlationID);
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
  public List<StudentEntity> lookupNoLocalID(String dob, String surname, String givenName, UUID correlationID) throws JsonProcessingException {
    LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    SearchCriteria criteriaDob = getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    List<SearchCriteria> criteriaListSurnameGiven = new LinkedList<>();
    if(StringUtils.isNotBlank(surname)) {
      criteriaListSurnameGiven.add(getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }
    if(StringUtils.isNotBlank(givenName)) {
      criteriaListSurnameGiven.add(getCriteriaWithCondition(LEGAL_FIRST_NAME, STARTS_WITH, givenName, STRING, AND));
    }

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if(!criteriaListSurnameGiven.isEmpty()){
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurnameGiven).build());
    }

    String criteriaJSON = objectMapper.writeValueAsString(searches);

    return getStudentsByCriteria(criteriaJSON, correlationID);
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
  private SearchCriteria getCriteria(String key, FilterOperation operation, String value, ValueType valueType) {
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
  private SearchCriteria getCriteriaWithCondition(String key, FilterOperation operation, String value, ValueType valueType, Condition condition) {
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
  public List<StudentEntity> lookupNoInitNoLocalID(String dob, String surname, UUID correlationID) throws JsonProcessingException {
    LocalDate dobDate = LocalDate.parse(dob, DOB_FORMATTER_SHORT);
    SearchCriteria criteriaDob = getCriteria(DOB, EQUAL, DOB_FORMATTER_LONG.format(dobDate), DATE);

    List<SearchCriteria> criteriaListDob = new LinkedList<>(Collections.singletonList(criteriaDob));

    List<SearchCriteria> criteriaListSurname = new LinkedList<>();
    if(StringUtils.isNotBlank(surname)) {
      criteriaListSurname.add(getCriteria(LEGAL_LAST_NAME, STARTS_WITH, surname, STRING));
    }

    List<Search> searches = new LinkedList<>();
    searches.add(Search.builder().searchCriteriaList(criteriaListDob).build());
    if(!criteriaListSurname.isEmpty()){
      searches.add(Search.builder().condition(OR).searchCriteriaList(criteriaListSurname).build());
    }

    String criteriaJSON = objectMapper.writeValueAsString(searches);
    return getStudentsByCriteria(criteriaJSON, correlationID);
  }

  /**
   * Fetches a PEN Master Record given a student number
   *
   * @param studentID the student id
   * @return the optional
   */
  public Optional<String> lookupStudentTruePENNumberByStudentID(String studentID) {
    RestTemplate restTemplate = getRestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    ParameterizedTypeReference<List<StudentMergeEntity>> type = new ParameterizedTypeReference<>() {
    };
    ResponseEntity<List<StudentMergeEntity>> studentResponse = restTemplate.exchange(props.getStudentApiURL() + "/" + studentID + "/merges?mergeDirection=TO", HttpMethod.GET, new HttpEntity<>(PARAMETERS_ATTRIBUTE, headers), type);

    if (studentResponse.hasBody()) {
      return Optional.ofNullable(StringUtils.trim(Objects.requireNonNull(studentResponse.getBody()).get(0).getMergeStudent().getPen()));
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
  public List<StudentEntity> getStudentsByCriteria(String criteria, UUID correlationID) {
    try {
      TypeReference<RestPageImpl<StudentEntity>> ref = new TypeReference<>() {
      };
      var obMapper = new ObjectMapper();
      Event event = Event.builder().sagaId(correlationID).eventType(GET_PAGINATED_STUDENT_BY_CRITERIA).eventPayload(SEARCH_CRITERIA_LIST.concat("=").concat(URLEncoder.encode(criteria, StandardCharsets.UTF_8)).concat("&").concat(PAGE_SIZE).concat("=").concat("100000")).build();
      var responseMessage = connection.request("STUDENT_API_TOPIC", obMapper.writeValueAsBytes(event), Duration.ofSeconds(60));
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
