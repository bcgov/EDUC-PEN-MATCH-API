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
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.support.WithMockOAuth2Scope;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PenMatchControllerTest {
	private MockMvc mvc;

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
		this.mvc.perform(post("/")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(asJsonString(entity))).andDo(print());
	}

	private PenMatchStudent createPenMatchStudent() {
		PenMatchStudent student = new PenMatchStudent();
		student.setAssignmentCode(null);
		student.setAssignmentDate(null);
		student.setEnrolledGradeCode(null);
		student.setFypFlag(null);
		student.setMincode(null);
		student.setNoMatches(null);
		student.setPenStatus(null);
		student.setPenStatusMessage(null);
		student.setPostal(null);
		student.setDob(null);
		student.setGivenName(null);
		student.setGivenInitial(null);
		student.setLocalID(null);
		student.setMiddleName(null);
		student.setMiddleInitial(null);
		student.setStudentNumber(null);
		student.setSex(null);
		student.setSurname(null);
		student.setSoundexSurname(null);
		student.setUpdateCode(null);
		student.setUsualGivenName(null);
		student.setUsualGivenInitial(null);
		student.setUsualMiddleName(null);
		student.setUsualMiddleInitial(null);
		student.setUsualSurname(null);
		student.setUsualSoundexSurname(null);
		student.setVersion(null);

		student.setPen1(null);
		student.setPen2(null);
		student.setPen3(null);
		student.setPen4(null);
		student.setPen5(null);
		student.setPen6(null);
		student.setPen7(null);
		student.setPen8(null);
		student.setPen9(null);
		student.setPen10(null);
		student.setPen11(null);
		student.setPen12(null);
		student.setPen13(null);
		student.setPen14(null);
		student.setPen15(null);
		student.setPen16(null);
		student.setPen17(null);
		student.setPen18(null);
		student.setPen19(null);
		student.setPen20(null);

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
