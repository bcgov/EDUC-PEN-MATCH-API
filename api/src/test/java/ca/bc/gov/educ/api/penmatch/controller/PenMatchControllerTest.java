package ca.bc.gov.educ.api.penmatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.File;
import java.util.List;

import ca.bc.gov.educ.api.penmatch.controller.v1.PenMatchController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.penmatch.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.support.WithMockOAuth2Scope;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class PenMatchControllerTest {

	private MockMvc mockMvc;

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	SurnameFrequencyRepository surnameFreqRepository;

	@Autowired
	PenMatchController controller;

	private static boolean dataLoaded = false;

	@Before
	public void setUp() throws Exception {
		if (!dataLoaded) {
			MockitoAnnotations.initMocks(this);
			mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new RestExceptionHandler()).build();
			final File file = new File("src/test/resources/mock_pen_demog.json");

			final File fileNick = new File("src/test/resources/mock_nicknames.json");
			List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<>() {
			});
			nicknamesRepository.saveAll(nicknameEntities);

			final File fileSurnameFreqs = new File("src/test/resources/mock_surname_frequency.json");
			List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFreqs, new TypeReference<>() {
			});
			surnameFreqRepository.saveAll(surnameFreqEntities);
			dataLoaded = true;
		}
	}

	@Test
	@WithMockOAuth2Scope(scope = "READ_PEN_MATCH")
	public void testCreateStudent_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
		PenMatchStudent entity = createPenMatchStudent();
		this.mockMvc.perform(post("/api/v1/pen-match").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(asJsonString(entity))).andDo(print()).andExpect(status().isOk());
	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setPen("122740046");
		student.setSurname("LORD");
		student.setGivenName("CLAYTON");
		student.setMiddleName("LUKE");
		student.setDob("19991201");
		student.setLocalID("285261");
		student.setSex("F");
		student.setMincode("00501007");

		return student;
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
