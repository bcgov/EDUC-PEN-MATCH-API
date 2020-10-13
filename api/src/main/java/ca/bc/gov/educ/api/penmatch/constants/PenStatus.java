package ca.bc.gov.educ.api.penmatch.constants;

/**
 * The enum Pen status.
 */
public enum PenStatus {
  /**
   * Aa pen status.
   */
  AA("AA"),
  /**
   * B pen status.
   */
  B("B"),
  /**
   * Bm pen status.
   */
  BM("BM"),
  /**
   * B 0 pen status.
   */
  B0("B0"),
  /**
   * B 1 pen status.
   */
  B1("B1"),
  /**
   * C pen status.
   */
  C("C"),
  /**
   * Cm pen status.
   */
  CM("CM"),
  /**
   * C 0 pen status.
   */
  C0("C0"),
  /**
   * C 1 pen status.
   */
  C1("C1"),
  /**
   * D pen status.
   */
  D("D"),
  /**
   * Dm pen status.
   */
  DM("DM"),
  /**
   * D 0 pen status.
   */
  D0("D0"),
  /**
   * D 1 pen status.
   */
  D1("D1"),
  /**
   * F pen status.
   */
  F("F"),
  /**
   * F 1 pen status.
   */
  F1("F1"),
  /**
   * G 0 pen status.
   */
  G0("G0"),
  /**
   * M pen status.
   */
  M("M"),
  /**
   * Ur pen status.
   */
  UR("UR");

  /**
   * The Status value.
   */
  private final String statusValue;

  /**
   * Instantiates a new Pen status.
   *
   * @param statusValue the status value
   */
  PenStatus(String statusValue) {
    this.statusValue = statusValue;
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  public String getValue() {
    return statusValue;
  }
}
