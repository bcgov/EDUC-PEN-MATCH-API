package ca.bc.gov.educ.api.penmatch.exception;

/**
 * The type lookup runtime exception.
 */
public class LookupRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 4413979549737000974L;

  /**
   * Instantiates a new lookup runtime exception.
   *
   * @param message the message
   */
  public LookupRuntimeException(String message) {
    super(message);
  }

}
