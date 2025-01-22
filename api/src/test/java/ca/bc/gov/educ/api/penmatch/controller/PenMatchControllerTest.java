package ca.bc.gov.educ.api.penmatch.controller;

import ca.bc.gov.educ.api.penmatch.constants.MatchReasonCodes;
import ca.bc.gov.educ.api.penmatch.controller.v1.PenMatchController;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.mappers.v1.PossibleMatchMapper;
import ca.bc.gov.educ.api.penmatch.model.v1.FrequencySurnameEntity;
import ca.bc.gov.educ.api.penmatch.model.v1.NicknameEntity;
import ca.bc.gov.educ.api.penmatch.repository.v1.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.PossibleMatchRepository;
import ca.bc.gov.educ.api.penmatch.repository.v1.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.service.v1.match.PossibleMatchWrapperService;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.v1.PossibleMatch;
import ca.bc.gov.educ.api.penmatch.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The type Pen match controller test.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PenMatchControllerTest {

  /**
   * The constant PEN.
   */
  public static final String PEN = "122740046";
  /**
   * The Rest utils.
   */
  @Autowired
  RestUtils restUtils;

  /**
   * The Rest template.
   */
  @MockBean
  RestTemplate restTemplate;

  /**
   * The Nicknames repository.
   */
  @Autowired
  NicknamesRepository nicknamesRepository;

  /**
   * The Surname freq repository.
   */
  @Autowired
  SurnameFrequencyRepository surnameFreqRepository;

  /**
   * The Controller.
   */
  @Autowired
  PenMatchController controller;
  /**
   * The Pen match lookup manager.
   */
  @Autowired
  PenMatchLookupManager penMatchLookupManager;

  /**
   * The Mock mvc.
   */
  @Autowired
  private MockMvc mockMvc;

  /**
   * The Possible match repository.
   */
  @Autowired
  PossibleMatchRepository possibleMatchRepository;

  /**
   * The Possible match service.
   */
  @Autowired
  private PossibleMatchWrapperService possibleMatchService;

  /**
   * The Correlation id.
   */
  UUID correlationID = UUID.randomUUID();

  /**
   * As json string string.
   *
   * @param obj the obj
   * @return the string
   */
  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    final File fileNick = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_nicknames.json")).getFile()
    );
    List<NicknameEntity> nicknameEntities = objectMapper.readValue(fileNick, new TypeReference<>() {
    });
    nicknamesRepository.saveAll(nicknameEntities);

    final File fileSurnameFrequency = new File(
        Objects.requireNonNull(getClass().getClassLoader().getResource("mock_surname_frequency.json")).getFile()
    );
    List<FrequencySurnameEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFrequency, new TypeReference<>() {
    });
    surnameFreqRepository.saveAll(surnameFreqEntities);
  }

  /**
   * After.
   */
  @After
  public void after() {
    possibleMatchRepository.deleteAll();
    nicknamesRepository.deleteAll();
    surnameFreqRepository.deleteAll();
  }

  /**
   * Test create student given valid payload should return status created.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPenMatch_GivenValidPayload_ShouldReturnMatchStatusC0() throws Exception {
    PenMatchStudent entity = createPenMatchStudent();
    when(restUtils.lookupWithAllParts("19991201", "LORD", "CLAYTON", "00501007", "285261", correlationID)).thenReturn(new ArrayList<>());
    when(restUtils.getPenMasterRecordByPen(PEN, correlationID)).thenReturn(Optional.of(new PenMasterRecord()));

    this.mockMvc.perform(post("/api/v1/pen-match").with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_MATCH"))).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(asJsonString(entity))).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.penStatus", is("C0")));

  }

  /**
   * Test nicknames for given name should return list of nicknames.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNicknames_ForGivenName_ShouldReturnListOfNicknames() throws Exception {
    GrantedAuthority grantedAuthority = () -> "SCOPE_READ_NICKNAMES";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    penMatchLookupManager.reloadCache();
    this.mockMvc
        .perform(get("/api/v1/pen-match/nicknames/ALEXANDER").with(mockAuthority).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
  }

  @Test
  public void testNicknames_ForGivenName_WithWrongScope_ShouldReturnStatusForbidden() throws Exception {
    GrantedAuthority grantedAuthority = () -> "WRONG_SCOPE";
    var mockAuthority = oidcLogin().authorities(grantedAuthority);
    penMatchLookupManager.reloadCache();
    this.mockMvc
        .perform(get("/api/v1/pen-match/nicknames/ALEXANDER").with(mockAuthority).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isForbidden());
  }

  /**
   * Test save possible matches given payload should return saved list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSavePossibleMatches_givenPayload_ShouldReturnSavedList() throws Exception {
    this.mockMvc
        .perform(post("/api/v1/pen-match/possible-match").with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_POSSIBLE_MATCH"))).content(asJsonString(getPossibleMatchesPlaceHolderData())).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$", hasSize(20)));
  }

  @Test
  public void testSavePossibleMatches_givenSamePayloadTwice_ShouldReturnEmptyList() throws Exception {
    var payload = asJsonString(getPossibleMatchesPlaceHolderData());
    this.mockMvc
        .perform(post("/api/v1/pen-match/possible-match").with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_POSSIBLE_MATCH"))).content(payload).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$", hasSize(20)));
    this.mockMvc
        .perform(post("/api/v1/pen-match/possible-match").with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_POSSIBLE_MATCH"))).content(payload).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$", hasSize(0)));
  }

  /**
   * Test get possible matches given valid student id should return list.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetPossibleMatches_givenValidStudentID_ShouldReturnList() throws Exception {
    var savedList = possibleMatchService.savePossibleMatches(getPossibleMatchesPlaceHolderData().stream().map(PossibleMatchMapper.mapper::toModel).collect(Collectors.toList()));
    this.mockMvc
        .perform(get("/api/v1/pen-match/possible-match/" + savedList.getLeft().get(0).getStudentID().toString()).with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_POSSIBLE_MATCH"))).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)));
  }

  /**
   * Test delete possible matches given valid student id should return no content.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeletePossibleMatches_givenValidStudentID_ShouldReturnNoContent() throws Exception {
    var savedList = possibleMatchService.savePossibleMatches(getPossibleMatchesPlaceHolderData().stream().map(PossibleMatchMapper.mapper::toModel).collect(Collectors.toList()));
    List<PossibleMatch> payload = new ArrayList<>();
    payload.add(PossibleMatch.builder().studentID(savedList.getLeft().get(0).getStudentID())
        .matchedStudentID(savedList.getLeft().get(0).getMatchedStudentID())
        .createUser("TEST")
        .updateUser("TEST").build());
    payload.add(PossibleMatch.builder().studentID(savedList.getLeft().get(2).getStudentID())
        .matchedStudentID(savedList.getLeft().get(2).getMatchedStudentID())
        .createUser("TEST")
        .updateUser("TEST").build());
    this.mockMvc
        .perform(delete("/api/v1/pen-match/possible-match").content(JsonUtil.getJsonStringFromObject(payload)).with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_POSSIBLE_MATCH"))).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isNoContent());
    assertThat(possibleMatchRepository.findAll().size()).isEqualTo(16);
  }

  /**
   * Test delete possible matches given invalid student id should return no content.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDeletePossibleMatches_givenInvalidStudentID_ShouldReturnNoContent() throws Exception {
    possibleMatchService.savePossibleMatches(getPossibleMatchesPlaceHolderData().stream().map(PossibleMatchMapper.mapper::toModel).collect(Collectors.toList()));
    List<PossibleMatch> payload = new ArrayList<>();
    payload.add(PossibleMatch.builder().studentID(UUID.randomUUID())
        .matchedStudentID(UUID.randomUUID())
        .createUser("TEST")
        .updateUser("TEST").build());
    this.mockMvc
        .perform(delete("/api/v1/pen-match/possible-match").content(JsonUtil.getJsonStringFromObject(payload)).with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_POSSIBLE_MATCH"))).contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isNoContent());
    assertThat(possibleMatchRepository.findAll().size()).isEqualTo(20);
  }


  /**
   * Create pen match student pen match student.
   *
   * @return the pen match student
   */
  private PenMatchStudent createPenMatchStudent() {
    PenMatchStudent student = new PenMatchStudent();
    student.setPen(PEN);
    student.setSurname("LORD");
    student.setGivenName("CLAYTON");
    student.setMiddleName("LUKE");
    student.setDob("19991201");
    student.setLocalID("285261");
    student.setSex("F");
    student.setMincode("00501007");

    return student;
  }

  /**
   * Gets possible matches place holder data.
   *
   * @return the possible matches place holder data
   */
  private List<PossibleMatch> getPossibleMatchesPlaceHolderData() {
    List<PossibleMatch> possibleMatches = new ArrayList<>();
    var studentID = UUID.randomUUID();
    for (int i = 0; i++ < 10; ) {
      possibleMatches.add(PossibleMatch.builder().studentID(studentID).matchedStudentID(UUID.randomUUID()).matchReasonCode(MatchReasonCodes.PENMATCH).build());
    }
    return possibleMatches;
  }

}
