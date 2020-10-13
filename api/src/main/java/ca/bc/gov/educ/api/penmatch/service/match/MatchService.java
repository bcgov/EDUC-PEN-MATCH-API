package ca.bc.gov.educ.api.penmatch.service.match;

/**
 * The interface Match service.
 *
 * @param <T> the type parameter the match payload.
 * @param <R> the type parameter the result of the match.
 */
public interface MatchService<T, R> {

  /**
   * Match student r.
   *
   * @param t the payload
   * @return R the result
   */
  R matchStudent(T t);
}
