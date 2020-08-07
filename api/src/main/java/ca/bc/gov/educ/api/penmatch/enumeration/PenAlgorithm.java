package ca.bc.gov.educ.api.penmatch.enumeration;
public enum PenAlgorithm 
{
    ALG_S1("S1"), 
    ALG_S2("S2"), 
    ALG_SP("SP"), 
	ALG_00("00"),
	ALG_20("20"),
	ALG_30("30"),
	ALG_40("40"),
	ALG_50("50"),
	ALG_51("51"),
	ALG_9999("9999");
 
    private String algorithmValue;
 
    PenAlgorithm(String algorithmValue) {
        this.algorithmValue = algorithmValue;
    }
 
    public String getValue() {
        return algorithmValue;
    }
}