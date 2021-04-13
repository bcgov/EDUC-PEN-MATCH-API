package ca.bc.gov.educ.api.penmatch.model.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * The type Student merge entity.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StudentMergeEntity implements Serializable {
  private static final long serialVersionUID = 2160199040305625786L;
  /**
   * The Create user.
   */
  public String createUser;
  /**
   * The Update user.
   */
  public String updateUser;
  String studentMergeID;
  /**
   * The Student id.
   */
  @NotNull(message = "Student ID can not be null.")
  String studentID;
  /**
   * The Merge student id.
   */
  @NotNull(message = "Merge Student ID can not be null.")
  String mergeStudentID;
  /**
   * The Student merge direction code.
   */
  @NotNull(message = "Student Merge Direction Code can not be null.")
  String studentMergeDirectionCode;
  /**
   * The Student merge source code.
   */
  @NotNull(message = "Student Merge Source Code can not be null.")
  String studentMergeSourceCode;
}
