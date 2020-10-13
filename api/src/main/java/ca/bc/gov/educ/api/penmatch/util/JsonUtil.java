package ca.bc.gov.educ.api.penmatch.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The type Json util.
 */
public class JsonUtil {
  /**
   * Instantiates a new Json util.
   */
  private JsonUtil() {
  }

  /**
   * Gets json string from object.
   *
   * @param payload the payload
   * @return the json string from object
   * @throws JsonProcessingException the json processing exception
   */
  public static String getJsonStringFromObject(Object payload) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(payload);
  }

  /**
   * Gets json object from string.
   *
   * @param <T>     the type parameter
   * @param clazz   the clazz
   * @param payload the payload
   * @return the json object from string
   * @throws JsonProcessingException the json processing exception
   */
  public static <T> T getJsonObjectFromString(Class<T> clazz, String payload) throws JsonProcessingException {
    return new ObjectMapper().readValue(payload, clazz);
  }

  /**
   * Gets json pretty string from object.
   *
   * @param payload the payload
   * @return the json pretty string from object
   */
  public static String getJsonPrettyStringFromObject(Object payload) {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      return "";
    }
  }
}
