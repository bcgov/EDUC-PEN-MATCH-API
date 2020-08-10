package ca.bc.gov.educ.api.penmatch.lookup;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.PenDemographicsRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PenMatchLookupManagerTest {

	@Autowired
	NicknamesRepository nicknamesRepository;

	@Autowired
	PenDemographicsRepository penDemographicsRepository;

	@Autowired
	SurnameFrequencyRepository surnameFrequencyRepository;

	@Autowired
	private EntityManager entityManager;

	PenMatchLookupManager lookupManager;

	@Before
	public void before() {
		lookupManager = new PenMatchLookupManager(entityManager, penDemographicsRepository, nicknamesRepository, surnameFrequencyRepository);
	}

	@Test
	public void testLookupSurnameFrequence_ShouldReturn0() {
		assertTrue(lookupManager.lookupSurnameFrequency("ASDFJSD") == 0);
	}
	
	@Test
	public void testLookupSurnameFrequence_ShouldReturnOver200() {
		assertTrue(lookupManager.lookupSurnameFrequency("AAS") > 200);
	}

}
