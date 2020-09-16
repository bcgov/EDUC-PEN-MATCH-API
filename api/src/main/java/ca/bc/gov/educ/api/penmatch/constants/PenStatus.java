package ca.bc.gov.educ.api.penmatch.constants;

public enum PenStatus {
  AA("AA"),
  B("B"),
  BM("BM"),
  B0("B0"),
  B1("B1"),
  C("C"),
  CM("CM"),
  C0("C0"),
  C1("C1"),
  D("D"),
  DM("DM"),
  D0("D0"),
  D1("D1"),
  F("F"),
  F1("F1"),
  G0("G0"),
  M("M"),
  UR("UR");

  private final String statusValue;

  PenStatus(String statusValue) {
    this.statusValue = statusValue;
  }

  public String getValue() {
    return statusValue;
  }
}
