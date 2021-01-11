package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Pen match event.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "PEN_MATCH_EVENT")
@Data
@DynamicUpdate
public class PENMatchEvent {
  /**
   * The Create user.
   */
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;
  /**
   * The Create date.
   */
  @Column(name = "CREATE_DATE", updatable = false)
  @PastOrPresent
  LocalDateTime createDate;
  /**
   * The Update user.
   */
  @Column(name = "UPDATE_USER")
  String updateUser;
  /**
   * The Update date.
   */
  @Column(name = "UPDATE_DATE")
  @PastOrPresent
  LocalDateTime updateDate;
  /**
   * The Event id.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID eventId;
  /**
   * The Event payload.
   */
  @NotNull(message = "eventPayload cannot be null")
  @Column(name = "EVENT_PAYLOAD")
  private String eventPayload;
  /**
   * The Event status.
   */
  @NotNull(message = "eventStatus cannot be null")
  @Column(name = "EVENT_STATUS")
  private String eventStatus;
  /**
   * The Event type.
   */
  @NotNull(message = "eventType cannot be null")
  @Column(name = "EVENT_TYPE")
  private String eventType;
  /**
   * The Saga id.
   */
  @Column(name = "SAGA_ID", updatable = false)
  private UUID sagaId;
  /**
   * The Event outcome.
   */
  @NotNull(message = "eventOutcome cannot be null.")
  @Column(name = "EVENT_OUTCOME")
  private String eventOutcome;
  /**
   * The Reply channel.
   */
  @Column(name = "REPLY_CHANNEL")
  private String replyChannel;
}
