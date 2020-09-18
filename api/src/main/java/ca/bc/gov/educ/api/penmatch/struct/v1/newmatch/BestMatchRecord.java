package ca.bc.gov.educ.api.penmatch.struct.v1.newmatch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BestMatchRecord {
    private Long matchValue;
    private String matchCode;
    private String matchPEN;
    private String studentID;
}
