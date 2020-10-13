package ca.bc.gov.educ.api.penmatch.exception;

import java.util.Map;

/**
 * InvalidValueException to provide error details when invalid value of a
 * parameter is passed to endpoint
 */
public class InvalidValueException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 8926815015510650437L;

  /**
   * Instantiates a new Invalid value exception.
   *
   * @param paramsMap the params map
   */
  public InvalidValueException(String... paramsMap) {
    super(InvalidValueException.generateMessage(
        ExceptionUtils.toMap(String.class, String.class, paramsMap)));
  }

  /**
   * Generate message string.
   *
   * @param values the values
   * @return the string
   */
  private static String generateMessage(Map<String, String> values) {
    String message = "Invalid request parameters provided: ";
    return message + values;
  }
}
