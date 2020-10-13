package ca.bc.gov.educ.api.penmatch.exception;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * The type Entity not found exception.
 */
public class EntityNotFoundException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 4413979549737000974L;

  /**
   * Instantiates a new Entity not found exception.
   *
   * @param clazz           the clazz
   * @param searchParamsMap the search params map
   */
  public EntityNotFoundException(Class<?> clazz, String... searchParamsMap) {
    super(EntityNotFoundException.generateMessage(clazz.getSimpleName(),
        ExceptionUtils.toMap(String.class, String.class, searchParamsMap)));
  }

  /**
   * Generate message string.
   *
   * @param entity       the entity
   * @param searchParams the search params
   * @return the string
   */
  private static String generateMessage(String entity, Map<String, String> searchParams) {
    return StringUtils.capitalize(entity) +
        " was not found for parameters " +
        searchParams;
  }
}
