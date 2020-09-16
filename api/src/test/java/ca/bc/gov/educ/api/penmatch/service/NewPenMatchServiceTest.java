package ca.bc.gov.educ.api.penmatch.service;

import ca.bc.gov.educ.api.penmatch.compare.NewPenMatchComparator;
import ca.bc.gov.educ.api.penmatch.constants.PenStatus;
import ca.bc.gov.educ.api.penmatch.lookup.PenMatchLookupManager;
import ca.bc.gov.educ.api.penmatch.model.NicknamesEntity;
import ca.bc.gov.educ.api.penmatch.model.SurnameFrequencyEntity;
import ca.bc.gov.educ.api.penmatch.repository.NicknamesRepository;
import ca.bc.gov.educ.api.penmatch.repository.SurnameFrequencyRepository;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchResult;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchRecord;
import ca.bc.gov.educ.api.penmatch.struct.v1.newmatch.NewPenMatchResult;
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
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.Assert.*;

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
    public void testMatchStudent_Alg30_ShouldReturnStatusD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen(null);
        student.setGivenName(null);
        student.setMiddleName("LUKE");
        student.setPostal("V1B1J0");
        student.setSex("M");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudent_Alg40_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen(null);
        student.setGivenName(null);
        student.setMiddleName(null);
        student.setSex("F");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudent_Alg50_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen(null);
        student.setGivenName(null);
        student.setMiddleName(null);
        student.setSex("M");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudent_WhenPayloadIsValidAlg51_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen(null);
        student.setGivenName("CLA");
        student.setMiddleName(null);
        student.setSex("F");
        student.setDob("19990501");
        student.setLocalID(null);
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudent_ShouldReturnAAMatch() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.AA.toString());
    }

    @Test
    public void testMatchStudentForCoreCheck_ShouldReturnC0() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailForCoreCheck();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.C0.toString());
    }

    @Test
    public void testMatchStudentValidTwin_ShouldReturnAA() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setLocalID("285262");
        student.setGivenName("CLAYTON");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.AA.toString());
    }

    @Test
    public void testMatchStudentInValidPEN_ShouldReturnC1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen("123456888");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.C1.toString());
    }

    @Test
    public void testMatchStudentWithInvalidPEN_ShouldReturnC1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetail();
        student.setPen("109508853");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.C1.toString());
    }

    @Test
    public void testMatchStudentWithoutPEN_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithoutPEN();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentNoMatches_ShouldReturnD0() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen(null);
        student.setSurname("PASCAL");
        student.setGivenName("JOSEPH");
        student.setMiddleName(null);
        student.setUsualSurname("PASCAL");
        student.setUsualGivenName("JOSEPH");
        student.setUsualMiddleName(null);
        student.setPostal(null);
        student.setDob("19601120");
        student.setLocalID("32342");
        student.setSex("X");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.D0.toString());
    }

    @Test
    public void testMatchStudentWithoutPENNoLocalID_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithoutPEN();
        student.setLocalID(null);
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithFull_ShouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithFullNoSex_ShouldReturnAAMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setSex(null);
        student.setDob("19991201");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.AA.toString());
    }

    @Test
    public void testMatchStudentWithFullSplitGiven_ShouldReturnF1() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setGivenName("LUKE JACK");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithFullSplitGivenDash_ShouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setGivenName("LUKE-JACK");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithUsualSplitGiven_shouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setUsualGivenName("LUKE JACK");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithUsualSmallSurname_ShouldReturnF1() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setSurname("LOR");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithFullSplitMiddleDash_ShouldReturnF1andPossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setUsualGivenName("luke-JACK");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithMergedRecord_ShouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setUsualGivenName("luke-JACK");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithNoSurname_ShouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createPenMatchFullStudent();
        student.setSurname(null);
        student.setUsualSurname(null);
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithMergedDeceased_ShouldReturnC0() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailMergedDeceased();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.C0.toString());
    }

    @Test
    public void testChangeResultFromQtoF() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailMergedValid();
        NewPenMatchSession session = new NewPenMatchSession();
        session.setMatchingRecords(new PriorityQueue<>(new NewPenMatchComparator()));
        session.getMatchingRecords().add(new NewPenMatchRecord("Q","1241112","12345678"));
        session.getMatchingRecords().add(new NewPenMatchRecord("100","1111222","87654321"));

        service.changeResultFromQtoF(student, session);
    }

    @Test
    public void testSumOfIntMatchCodes() {
        service.getSumOfMatchCode("1111222");
    }

    @Test
    public void testMatchStudentWithMergedValid_ShouldReturnF1PossibleMatch() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailMergedValid();

        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertTrue(result.getMatchingRecords().size() > 0);
        assertEquals(result.getPenStatus(), PenStatus.F1.toString());
    }

    @Test
    public void testMatchStudentWithMergedValidComplete_ShouldReturnB1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailMergedValidComplete();

        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.B1.toString());
    }

    @Test
    public void testMatchStudentWithRareName_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        ObjectMapper mapper = new ObjectMapper();

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameMiddleFlip_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setMiddleName("VICTORIA");
        student.setGivenName("WILLIAM");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeS_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setMincode("00501007");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWrongGiven_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setGivenName("PETE");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWrongMiddle_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setMiddleName("YARN");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWrongLocalID_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setLocalID("239661");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());

    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWrongSurname_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setSurname("JAKE");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWrongSex_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setSex("F");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWithWrongPostal_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setPostal("V0B1R2");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWithWrongDob_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setDob("19920223");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSValid_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        ObjectMapper mapper = new ObjectMapper();

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeSWithPostal_ShouldReturnD1Match() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailWithRareName();
        student.setUpdateCode("S");
        student.setPostal("V0B1R0");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());

        assertEquals(result.getPenStatus(), PenStatus.D1.toString());
    }

    @Test
    public void testMatchStudentWithRareNameWithUpdateCodeY_ShouldReturnC0() {
        NewPenMatchStudentDetail student = createNewPenMatchStudentDetailMergedDeceased();
        student.setUpdateCode("Y");
        NewPenMatchResult result = service.matchStudent(student);
        assertNotNull(result);
        assertNotNull(result.getPenStatus());
        assertEquals(result.getPenStatus(), PenStatus.C0.toString());
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

    private NewPenMatchStudentDetail createPenMatchFullStudent() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("122740046");
        student.setSurname("LORD");
        student.setGivenName("CLAYTON");
        student.setMiddleName("L");
        student.setDob("19990112");
        student.setLocalID("285261");
        student.setSex("F");

        student.setMincode("00501007");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailWithoutPEN() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setSurname("LORD");
        student.setGivenName("CLAYTON");
        student.setMiddleName("L");
        student.setDob("19991201");
        student.setLocalID("285261");
        student.setSex("F");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailWithRareName() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setSurname("ODLUS");
        student.setGivenName("VICTORIA");
        student.setMiddleName("WILLIAM");
        student.setDob("19981102");
        student.setLocalID("239661");
        student.setSex("M");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailMergedDeceased() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("108999400");
        student.setSurname("VANDERLEEK");
        student.setGivenName("JAKE");
        student.setMiddleName("WILLIAM");
        student.setUsualSurname("VANDERLEEK");
        student.setUsualGivenName("JAKE");
        student.setUsualMiddleName("WILLIAM");
        student.setDob("19791018");
        student.setLocalID("285261");
        student.setSex("M");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailMergedValid() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("113874044");
        student.setSurname("SMITH");
        student.setGivenName("JOE");
        student.setMiddleName("JAMES");
        student.setUsualSurname("SMITH");
        student.setUsualGivenName("JOE");
        student.setUsualMiddleName("JAMES");
        student.setDob("19800410");
        student.setSex("M");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailMergedValidComplete() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("113874044");
        student.setSurname("SMITH");
        student.setGivenName("JOE");
        student.setMiddleName("JAMES");
        student.setUsualSurname("SMITH");
        student.setUsualGivenName("JOE");
        student.setUsualMiddleName("JAMES");
        student.setDob("19800412");
        student.setSex("M");

        return student;
    }

    private NewPenMatchStudentDetail createNewPenMatchStudentDetailForCoreCheck() {
        NewPenMatchStudentDetail student = new NewPenMatchStudentDetail();
        student.setPen("113874041");
        student.setSurname("VANDERLEEK");
        student.setGivenName("JAKE");
        student.setMiddleName("WILLIAM");
        student.setUsualSurname("VANDERLEEK");
        student.setUsualGivenName("JAKE");
        student.setUsualMiddleName("WILLIAM");
        student.setPostal(null);
        student.setDob("19791018");
        student.setLocalID("285261");
        student.setSex("M");
        student.setMincode("08288006");
        student.setUpdateCode("Y");

        return student;
    }

}
