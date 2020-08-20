package ca.bc.gov.educ.api.penmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.penmatch.endpoint.PenMatchEndpoint;
import ca.bc.gov.educ.api.penmatch.mappers.PenMatchStudentMapper;
import ca.bc.gov.educ.api.penmatch.service.PenMatchService;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudentDetail;
import lombok.AccessLevel;
import lombok.Getter;

@RestController
@EnableResourceServer
public class PenMatchController implements PenMatchEndpoint {
	@Getter(AccessLevel.PRIVATE)
	private final PenMatchService penMatchService;

	private static final PenMatchStudentMapper mapper = PenMatchStudentMapper.mapper;

	@Autowired
	public PenMatchController(final PenMatchService penMatchService) {
		this.penMatchService = penMatchService;
	}

	@Override
	public PenMatchResult matchStudent(PenMatchStudent student) {
		PenMatchStudentDetail studentDetail = new PenMatchStudentDetail();
		return penMatchService.matchStudent(mapper.toPenMatchStudentDetails(student));
	}

}
