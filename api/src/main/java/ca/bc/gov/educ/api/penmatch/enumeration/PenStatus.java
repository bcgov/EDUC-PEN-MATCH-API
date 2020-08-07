package ca.bc.gov.educ.api.penmatch.enumeration;
public enum PenStatus 
{	
    AA("AA"), 
    B("B"), 
    B1("B1"), 
	C("C"),
	C0("C0"),
	C1("C1"),
	D("D"),
	D0("D0"),
	D1("D1"),
	F("F"),
	F1("F1"),
	G0("G0"),
	M("M");
 
    private String statusValue;
 
    PenStatus(String statusValue) {
        this.statusValue = statusValue;
    }
 
    public String getValue() {
        return statusValue;
    }
}