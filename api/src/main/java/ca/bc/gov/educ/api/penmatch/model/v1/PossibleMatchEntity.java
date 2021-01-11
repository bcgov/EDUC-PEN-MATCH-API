package ca.bc.gov.educ.api.penmatch.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * The type Possible match entity.
 */
@Entity
@Table(name = "POSSIBLE_MATCH")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class PossibleMatchEntity {
  /**
   * The Possible match id.
   */
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "POSSIBLE_MATCH_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID possibleMatchID;

  /**
   * The Student id.
   */
  @NotNull(message = "studentID cannot be null")
  @Column(name = "STUDENT_ID")
  UUID studentID;

  /**
   * The Matched student id.
   */
  @NotNull(message = "matched student cannot be null")
  @Column(name = "MATCHED_STUDENT_ID")
  UUID matchedStudentID;

  /**
   * The Match reason code.
   */
  @NotNull(message = "matchReasonCode cannot be null")
  @Column(name = "MATCH_REASON_CODE")
  String matchReasonCode;

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
}