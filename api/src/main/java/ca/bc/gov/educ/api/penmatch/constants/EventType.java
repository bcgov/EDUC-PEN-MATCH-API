package ca.bc.gov.educ.api.penmatch.constants;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Process pen match event type.
   */
  PROCESS_PEN_MATCH,
  /**
   * Pen match event outbox processed event type.
   */
  PEN_MATCH_EVENT_OUTBOX_PROCESSED,
  /**
   * Get student event type.
   */
  GET_STUDENT,
  /**
   * Get paginated student by criteria event type.
   */
  GET_PAGINATED_STUDENT_BY_CRITERIA,
  /**
   * Add possible match event type.
   */
  ADD_POSSIBLE_MATCH,
  /**
   * Get possible match event type.
   */
  GET_POSSIBLE_MATCH,
  /**
   * Delete possible match event type.
   */
  DELETE_POSSIBLE_MATCH
}
