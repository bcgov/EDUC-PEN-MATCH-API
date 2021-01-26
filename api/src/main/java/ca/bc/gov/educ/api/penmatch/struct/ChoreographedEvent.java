package ca.bc.gov.educ.api.penmatch.struct;

import ca.bc.gov.educ.api.penmatch.constants.EventOutcome;
import ca.bc.gov.educ.api.penmatch.constants.EventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * The type Choreographed event.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChoreographedEvent extends Event {
  /**
   * The Event id.
   */
  String eventID; // the primary key of event table.

  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;

  /**
   * Choreographed event builder choreographed event . choreographed event builder.
   *
   * @return the choreographed event . choreographed event builder
   */
  public static ChoreographedEvent.ChoreographedEventBuilder choreographedEventBuilder() {
    return new ChoreographedEvent.ChoreographedEventBuilder();
  }

  /**
   * The type Choreographed event builder.
   */
  public static class ChoreographedEventBuilder {
    /**
     * The Event id.
     */
    String eventID; // the primary key of event table.

    /**
     * The Event type.
     */
    EventType eventType;
    /**
     * The Event outcome.
     */
    EventOutcome eventOutcome;
    /**
     * The Saga id.
     */
    UUID sagaId;
    /**
     * The Reply to.
     */
    String replyTo;
    /**
     * The Event payload.
     */
    String eventPayload; // json string

    /**
     * Event type choreographed event . choreographed event builder.
     *
     * @param eventType the event type
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder eventType(final EventType eventType) {
      this.eventType = eventType;
      return this;
    }

    /**
     * Event outcome choreographed event . choreographed event builder.
     *
     * @param eventOutcome the event outcome
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder eventOutcome(final EventOutcome eventOutcome) {
      this.eventOutcome = eventOutcome;
      return this;
    }

    /**
     * Saga id choreographed event . choreographed event builder.
     *
     * @param sagaId the saga id
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder sagaId(final UUID sagaId) {
      this.sagaId = sagaId;
      return this;
    }

    /**
     * Reply to choreographed event . choreographed event builder.
     *
     * @param replyTo the reply to
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder replyTo(final String replyTo) {
      this.replyTo = replyTo;
      return this;
    }

    /**
     * Event payload choreographed event . choreographed event builder.
     *
     * @param eventPayload the event payload
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder eventPayload(final String eventPayload) {
      this.eventPayload = eventPayload;
      return this;
    }

    /**
     * Event id choreographed event . choreographed event builder.
     *
     * @param eventID the event id
     * @return the choreographed event . choreographed event builder
     */
    public ChoreographedEvent.ChoreographedEventBuilder eventID(final String eventID) {
      this.eventID = eventID;
      return this;
    }

    /**
     * Build choreographed event.
     *
     * @return the choreographed event
     */
    public ChoreographedEvent build() {
      ChoreographedEvent choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventID(this.eventID);
      choreographedEvent.setEventOutcome(this.eventOutcome);
      choreographedEvent.setEventPayload(this.eventPayload);
      choreographedEvent.setEventType(this.eventType);
      choreographedEvent.setReplyTo(this.replyTo);
      choreographedEvent.setSagaId(this.sagaId);
      return choreographedEvent;
    }

    public String toString() {
      return "ChoreographedEvent.ChoreographedEventBuilder(eventType=" + this.eventType + ", eventOutcome=" + this.eventOutcome + ", sagaId=" + this.sagaId + ", replyTo=" + this.replyTo + ", eventPayload=" + this.eventPayload + ", eventID=" + this.eventID + ")";
    }
  }

}
