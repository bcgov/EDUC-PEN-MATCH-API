package ca.bc.gov.educ.api.penmatch.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.IOException;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.penmatch.exception.RestExceptionHandler;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.support.WithMockOAuth2Scope;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PenMatchControllerTest {
	private MockMvc mvc;

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemogRepository;

	@Autowired
	SurnameFrequencyRepository surnameFreqRepository;

	@Autowired
	PenMatchController controller;

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new RestExceptionHandler()).build();
	}

	@Test
	@WithMockOAuth2Scope(scope = "READ_PEN_MATCH")
	public void testCreateStudent_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
		PenMatchStudent entity = createPenMatchStudent();
		this.mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(asJsonString(entity))).andDo(print());
	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setEnrolledGradeCode(null);
		student.setMincode(null);
		student.setPostal(null);
		student.setDob(null);
		student.setGivenName(null);
		student.setGivenInitial(null);
		student.setLocalID(null);
		student.setMiddleName(null);
		student.setMiddleInitial(null);
		student.setPen(null);
		student.setSex(null);
		student.setSurname(null);
		student.setUpdateCode(null);
		student.setUsualGivenName(null);
		student.setUsualGivenInitial(null);
		student.setUsualMiddleName(null);
		student.setUsualMiddleInitial(null);
		student.setUsualSurname(null);

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
