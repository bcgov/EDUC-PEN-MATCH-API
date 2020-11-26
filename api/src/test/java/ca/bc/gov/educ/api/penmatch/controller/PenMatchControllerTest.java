package ca.bc.gov.educ.api.penmatch.controller;

import ca.bc.gov.educ.api.penmatch.controller.v1.PenMatchController;
import ca.bc.gov.educ.api.penmatch.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.rest.RestUtils;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMasterRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.support.WithMockOAuth2Scope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchControllerTest {

  public static final String PEN = "122740046";
  private static boolean dataLoaded = false;
  @Autowired
  RestUtils restUtils;

  @MockBean
  RestTemplate restTemplate;

  @Autowired
  NicknamesRepository nicknamesRepository;

  @Autowired
  SurnameFrequencyRepository surnameFreqRepository;

  @Autowired
  PenMatchController controller;
  private MockMvc mockMvc;

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void setUp() throws Exception {
    if (!dataLoaded) {
      MockitoAnnotations.initMocks(this);
      mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new RestExceptionHandler()).build();

      final File fileNick = new File(
              Objects.requireNonNull(getClass().getClassLoader().getResource("mock_nicknames.json")).getFile()
      );
      List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<>() {
      });
      nicknamesRepository.saveAll(nicknameEntities);

      final File fileSurnameFrequency = new File(
              Objects.requireNonNull(getClass().getClassLoader().getResource("mock_surname_frequency.json")).getFile()
      );
      List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFrequency, new TypeReference<>() {
      });
      surnameFreqRepository.saveAll(surnameFreqEntities);
      dataLoaded = true;
    }
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_MATCH")
  public void testCreateStudent_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    PenMatchStudent entity = createPenMatchStudent();
    when(restUtils.getRestTemplate()).thenReturn(restTemplate);
    when(restUtils.lookupWithAllParts("19991201", "LORD", "CLAYTON", "00501007", "285261")).thenReturn(new ArrayList<>());
    when(restUtils.getPenMasterRecordByPen(PEN)).thenReturn(Optional.of(new PenMasterRecord()));

    MvcResult result = mockMvc
        .perform(post("/api/v1/pen-match").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(asJsonString(entity)))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.penStatus", is("C0")));

  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_NICKNAMES")
  public void testNicknames_ForGivenName_ShouldReturnListOfNicknames() throws Exception {
    PenMatchStudent entity = createPenMatchStudent();
    when(restUtils.getRestTemplate()).thenReturn(restTemplate);

    mockMvc
        .perform(get("/api/v1/pen-match/nicknames/ALEXANDER").contentType(MediaType.APPLICATION_JSON))
        .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
  }

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

}
