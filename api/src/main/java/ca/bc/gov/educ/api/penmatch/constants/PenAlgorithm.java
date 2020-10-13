package ca.bc.gov.educ.api.penmatch.constants;

/**
 * The enum Pen algorithm.
 */
public enum PenAlgorithm {
  /**
   * Alg s 1 pen algorithm.
   */
  ALG_S1("S1"),
  /**
   * Alg s 2 pen algorithm.
   */
  ALG_S2("S2"),
  /**
   * Alg sp pen algorithm.
   */
  ALG_SP("SP"),
  /**
   * Alg 00 pen algorithm.
   */
  ALG_00("00"),
  /**
   * Alg 20 pen algorithm.
   */
  ALG_20("20"),
  /**
   * Alg 30 pen algorithm.
   */
  ALG_30("30"),
  /**
   * Alg 40 pen algorithm.
   */
  ALG_40("40"),
  /**
   * Alg 50 pen algorithm.
   */
  ALG_50("50"),
  /**
   * Alg 51 pen algorithm.
   */
  ALG_51("51"),
  /**
   * Alg 9999 pen algorithm.
   */
  ALG_9999("9999");

  /**
   * The Algorithm value.
   */
  private final String algorithmValue;

  /**
   * Instantiates a new Pen algorithm.
   *
   * @param algorithmValue the algorithm value
   */
  PenAlgorithm(String algorithmValue) {
    this.algorithmValue = algorithmValue;
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  public String getValue() {
    return algorithmValue;
  }
}
