package ca.bc.gov.educ.api.penmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.struct.PenMatchStudent;
import lombok.AccessLevel;
import lombok.Getter;

@Service
public class PenMatchService {

	@Getter(AccessLevel.PRIVATE)
	private final PenDemographicsRepository penDemographicsRepository;

	@Autowired
	public PenMatchService(final PenDemographicsRepository penDemographicsRepository) {
		this.penDemographicsRepository = penDemographicsRepository;
	}

	public PenMatchStudent matchStudent(PenMatchStudent student) {
		// Run algorithm
		return null;
	}

}
