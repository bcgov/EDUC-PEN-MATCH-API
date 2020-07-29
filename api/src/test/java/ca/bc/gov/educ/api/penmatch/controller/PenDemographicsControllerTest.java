package ca.bc.gov.educ.api.pendemog.controller;

import ca.bc.gov.educ.api.pendemog.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.pendemog.mappers.PenDemographicsMapper;
import ca.bc.gov.educ.api.pendemog.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.pendemog.struct.PenDemographics;
import ca.bc.gov.educ.api.pendemog.support.WithMockOAuth2Scope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings("squid:S00100")
public class PenDemographicsControllerTest {
  private MockMvc mvc;
  private static final PenDemographicsMapper mapper = PenDemographicsMapper.mapper;
  @Autowired
  PenDemographicsRepository repository;
  @Autowired
  PenDemographicsController controller;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
    mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler()).build();
    final File file = new File(
            Objects.requireNonNull(getClass().getClassLoader().getResource("PenDemogDummyData.json")).getFile()
    );
    List<PenDemographics> entities = new ObjectMapper().readValue(file, new TypeReference<List<PenDemographics>>() {
    });
    repository.saveAll(entities.stream().map(penDemographics -> {
      if (penDemographics.getStudGiven() != null) {
        penDemographics.setStudGiven(penDemographics.getStudGiven().toUpperCase());
      }
      if (penDemographics.getStudSurname() != null) {
        penDemographics.setStudSurname(penDemographics.getStudSurname().toUpperCase());
      }
      if (penDemographics.getStudMiddle() != null) {
        penDemographics.setStudMiddle(penDemographics.getStudMiddle().toUpperCase());
      }
      if (penDemographics.getStudSex() != null) {
        penDemographics.setStudSex(penDemographics.getStudSex().toUpperCase());
      }
      if (penDemographics.getStudBirth() != null) {
        penDemographics.setStudBirth(penDemographics.getStudBirth().replaceAll("-", ""));
      }
      return mapper.toModel(penDemographics);
    }).collect(Collectors.toList()));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testGetPenDemographicsByPen_GivenPenExistInDB_ShouldReturnStatusOk() throws Exception {

    this.mvc.perform(get("/7613009916")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.pen", is("7613009916")))
            .andExpect(jsonPath("$.studSurname", is("Fratczak".toUpperCase())))
            .andExpect(jsonPath("$.studGiven", is("Jaquelin".toUpperCase())))
            .andExpect(jsonPath("$.studBirth", is("20010519")));

  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testGetPenDemographicsByPen_GivenPenDoesNotExistInDB_ShouldReturnStatusNotFound() throws Exception {

    this.mvc.perform(get("/7613009911")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isNotFound());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testSearchPenDemographics_GivenNoQueryParameters_ShouldReturnStatusOkWithNoResults() throws Exception {

    this.mvc.perform(get("/")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }


  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testSearchPenDemographics_GivenOnlySexInQueryParam_ShouldReturnStatusOkAndNoResults() throws Exception {

    this.mvc.perform(get("/?studSex=Female")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testSearchPenDemographics_GivenOnlyDOBInQueryParam_ShouldReturnStatusOk() throws Exception {

    this.mvc.perform(get("/?studBirth=20010519")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testSearchPenDemographics_GivenWrongFormatDOBInQueryParam_ShouldReturnStatusBadRequest() throws Exception {

    this.mvc.perform(get("/?studBirth=2001-05-19")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockOAuth2Scope(scope = "READ_PEN_DEMOGRAPHICS")
  public void testSearchPenDemographics2_GivenWrongFormatDOBInQueryParam_ShouldReturnStatusBadRequest() throws Exception {

    this.mvc.perform(get("/?studBirth=00000000")
            .accept(MediaType.APPLICATION_JSON)).andDo(print())
            .andExpect(status().isBadRequest());
  }

}
