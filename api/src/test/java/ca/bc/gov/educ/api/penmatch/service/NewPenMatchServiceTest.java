package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.compare.NewPenMatchComparator;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchSession;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchStudentDetail;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class NewPenMatchServiceTest {

    private static NewPenMatchService service;

    @Autowired
    NicknamesRepository nicknamesRepository;

    @Autowired
    SurnameFrequencyRepository surnameFreqRepository;

    @MockBean
    PenMatchLookupManager lookupManager;

    private static boolean dataLoaded = false;

    @Before
    public void setup() throws Exception {
        if (!dataLoaded) {
            service = new NewPenMatchService(lookupManager);

            final File fileNick = new File("src/test/resources/mock_nicknames.json");
            List<NicknamesEntity> nicknameEntities = new ObjectMapper().readValue(fileNick, new TypeReference<List<NicknamesEntity>>() {
            });
            nicknamesRepository.saveAll(nicknameEntities);

            final File fileSurnameFreqs = new File("src/test/resources/mock_surname_frequency.json");
            List<SurnameFrequencyEntity> surnameFreqEntities = new ObjectMapper().readValue(fileSurnameFreqs, new TypeReference<List<SurnameFrequencyEntity>>() {
            });
            surnameFreqRepository.saveAll(surnameFreqEntities);
            dataLoaded = true;
        }
    }

    @Test
    public void testChangeResultFromQtoF() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        NewPenMatchSession session = new NewPenMatchSession();
        session.setMatchingRecords(new PriorityQueue<>(new NewPenMatchComparator()));
        session.getMatchingRecords().add(new NewPenMatchRecord("Q", "1241112", "12345678", "12321"));
        session.getMatchingRecords().add(new NewPenMatchRecord("100", "1111222", "87654321","34343"));

        service.changeResultFromQtoF(student, session);
    }

    @Test
    public void testSumOfIntMatchCodes() {
        service.getSumOfMatchCode("1111222");
    }

    @Test
    public void testOneCharTypoTrue() {
        assertTrue(service.oneCharTypo("MARCO", "MAARCO" ));
    }

    @Test
    public void testOneCharTypoLengthFalse() {
        assertFalse(service.oneCharTypo("MARCO", "MAAARCO" ));
    }

    @Test
    public void testOneCharTypoFalse() {
        assertFalse(service.oneCharTypo("MARCO", "JAMES" ));
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetail() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("122740046");
        student.setSurname("LORD");
        student.setGivenName("CLAYTON");
        student.setMiddleName("L");
        student.setDob("19991201");
        student.setLocalID("285261");
        student.setSex("F");
        student.setMincode("00501007");

        return student;
    }

}
