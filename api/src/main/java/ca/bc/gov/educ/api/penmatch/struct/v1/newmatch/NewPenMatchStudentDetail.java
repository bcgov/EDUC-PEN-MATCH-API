package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewPenMatchStudentDetail extends PenMatchStudentDetail {

    //These are updated by the match algorithm
    private PenMatchNames penMatchTransactionNames;
    private String alternateLocalID;
    private Integer minSurnameSearchSize;
    private Integer maxSurnameSearchSize;
    private String partialStudentSurname;
    private String partialStudentGiven;
    private Integer fullSurnameFrequency;
    private Integer partialSurnameFrequency;
    private String studentTrueNumber;
    private String oldMatchF1PEN;
}
