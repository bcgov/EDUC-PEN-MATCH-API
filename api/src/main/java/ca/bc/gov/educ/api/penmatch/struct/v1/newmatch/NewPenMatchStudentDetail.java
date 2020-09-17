package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;


import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchNames;
import ca.bc.gov.educ.api.penmatch.struct.v1.PenMatchStudentDetail;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewPenMatchStudentDetail extends PenMatchStudentDetail {

    //These are updated by the match algorithm
    private String studentTrueNumber;
    private String oldMatchF1PEN;
    private String oldMatchF1StudentID;

    @Builder
    public NewPenMatchStudentDetail(PenMatchStudentDetail studentDetail, String oldMatchF1PEN, String oldMatchF1StudentID){
        this.pen = studentDetail.getPen();
        this.dob = studentDetail.getDob();
        this.sex = studentDetail.getSex();
        this.enrolledGradeCode = studentDetail.getEnrolledGradeCode();
        this.surname = studentDetail.getSurname();
        this.givenName= studentDetail.getGivenName();
        this.middleName = studentDetail.getMiddleName();
        this.usualSurname =studentDetail.getUsualSurname();
        this.usualGivenName = studentDetail.getUsualGivenName();
        this.usualMiddleName = studentDetail.getUsualMiddleName();
        this.mincode = studentDetail.getMincode();
        this.localID = studentDetail.getLocalID();
        this.postal = studentDetail.getPostal();
        this.updateCode = studentDetail.getUpdateCode();
        this.oldMatchF1PEN = oldMatchF1PEN;
        this.oldMatchF1StudentID = oldMatchF1StudentID;
    }



}
